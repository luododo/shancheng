package com.atguigu.gulimail.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimail.order.constant.OrderConstant;
import com.atguigu.gulimail.order.entity.OrderItemEntity;
import com.atguigu.gulimail.order.feign.CartFeginService;
import com.atguigu.gulimail.order.feign.MemberFeginService;
import com.atguigu.gulimail.order.feign.ProductFeignService;
import com.atguigu.gulimail.order.feign.WmsFeignService;
import com.atguigu.gulimail.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimail.order.to.OrderCreateTo;
import com.atguigu.gulimail.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.order.dao.OrderDao;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    //将页面传递过来的数据进行共享
    private ThreadLocal<OrderSubmitVo> submitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    CartFeginService cartFeginService;

    @Autowired
    MemberFeginService memberFeginService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.远程客户地址列表
            //每个线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeginService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> getCartFuture = CompletableFuture.runAsync(() -> {
            //2.远程购物车所有选中的购物项
            //每个线程共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeginService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(collect1);
            }
        }, executor);
        //3.查询用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4.防重复提交令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setToken(token);
        CompletableFuture.allOf(getAddressFuture, getCartFuture).get();
        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        submitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.loginUser.get();
        //1.验证令牌(令牌的对比和删除必须保证原子性)
        //如果调用get方法与传入的val相同，调用del方法，不相同返回0;0失败，1成功
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //Lua脚本
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);
        //String s = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        if (result == 0) {
            //失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //成功

            OrderCreateTo order = createOrder();
            return null;
        }
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId();
        //执行创建订单方法
        OrderEntity orderEntity = buildOrder(orderSn);
        //得到所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //验价
        return orderCreateTo;
    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        //获取收货地址信息
        OrderSubmitVo submitVo = submitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo data = fare.getData(new TypeReference<FareVo>() {
        });
        //设置运费信息
        entity.setFreightAmount(data.getFare());
        //设置收货信息
        entity.setReceiverCity(data.getAddress().getCity());
        entity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
        entity.setReceiverName(data.getAddress().getName());
        entity.setBillReceiverPhone(data.getAddress().getPhone());
        entity.setReceiverPostCode(data.getAddress().getPostCode());
        entity.setReceiverProvince(data.getAddress().getProvince());
        entity.setReceiverRegion(data.getAddress().getRegion());
        return entity;
    }

    /**
     * 构建所有订单项
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //获取所有订单项,最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeginService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(cartItem -> {
                //构建订单项数据
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    /**
     * 构建某一个订单项
     *
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1.订单信息:订单号
        //2.商品sup信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //3.商品sku信息
        itemEntity.setSkuId(skuId);
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4.优惠信息
        //5.积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        return itemEntity;
    }
}