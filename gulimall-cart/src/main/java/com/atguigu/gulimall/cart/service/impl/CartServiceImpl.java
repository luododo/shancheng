package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    private final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //商品添加到购物车
        CartItem cartItem = new CartItem();
        //远程查询当前要添加的商品信息
        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            R skuInfo = productFeignService.getSkuInfo(skuId);
            SkuInfoVo data = skuInfo.getData2("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(data.getSkuDefaultImg());
            cartItem.setTitle(data.getSkuTitle());
            cartItem.setSkuId(skuId);
            cartItem.setPrice(data.getPrice());
        }, executor);
        //远程查询sku的组合信息
        CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
            List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
            cartItem.setSkuAttr(values);
        }, executor);
        //把cartItem对象转换为json格式
        CompletableFuture.allOf(getSkuInfoTask,getSkuSaleAttrValues).get();
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
        return cartItem;
    }

    /*
    获取到要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1.判断真实用户还是临时用户
        String cartKry = "";
        if (userInfoTo.getUserId() != null) {
            cartKry = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKry = CART_PREFIX + userInfoTo.getUserKey();
        }
        //绑定key,以后所有redis操作都针对此key
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(cartKry);
        return operation;
    }
}
