package com.atguigu.gulimall.gulimallseckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.gulimallseckill.feign.CouponFeignService;
import com.atguigu.gulimall.gulimallseckill.feign.ProductFeignService;
import com.atguigu.gulimall.gulimallseckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.gulimallseckill.service.SeckillService;
import com.atguigu.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.gulimallseckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.gulimallseckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码


    /**
     * 上架秒杀商品
     */
    @Override
    public void upSeckillSkuLatest3Days() {
        //1.扫描需要参与秒杀的活动
        R session = couponFeignService.getLates3DaySession();
        if (session.getCode() == 0) {
            //上架商品
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis
            //1.缓存活动信息
            saveSessionInfos(sessionData);
            //2.缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    /**
     * 查询当前时间可以秒杀的商品
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1.当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            Long start = Long.parseLong(s[0]);
            Long end = Long.parseLong(s[1]);
            if (time >= start && time <= end) {
                //2.获取这个秒杀场次需要的所有商品信息
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                        SeckillSkuRedisTo redis = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
//                       redis.setRandomCode(null);
                        return redis;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    /**
     * 查询当前商品是否参与秒杀
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1.找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        //正则，先获取所有key（1_2）然后在服务器代码中进行正则验证
        String regx = "\\d_" + skuId;
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                //正则匹配
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    //随机码处理
                    long currentTime = new Date().getTime();
                    if (!(currentTime >= redisTo.getStartTime() && currentTime <= redisTo.getEndTime())) {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();
        //1.获取当前秒杀商品详细信息
        BoundHashOperations<String, String, String> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isEmpty(json)){
            return null;
        }else {
            //2.判断合法性校验（拦截器已判断是否登录)
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //2.1校验时间合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long time = new Date().getTime();
            long ttl = endTime - time;//毫秒
            if(time >= startTime && time <= endTime){
                //2.2校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String id = redisTo.getPromotionSessionId()+"_"+redisTo.getSkuId();
                if(randomCode.equals(key) && killId.equals(id)){
                    //2.3验证购买数量
                    if(num <= redisTo.getSeckillLimit()){
                        //2.4验证是否购买过商品(幂等性)
                        //setnx不存在的时候才占位
                        String redisKey = respVo.getId()+"_"+id;
                        //自动过期
                        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if(aBoolean){
                            //成功表示用户第一次购买
                            //3.获取信号量tryAcquire()非阻塞方法
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 300, TimeUnit.MILLISECONDS);
                                //秒杀成功，快速下单,返回订单号
                                String timeId = IdWorker.getTimeId();
                                //4.向队列发送消息
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",orderTo);
                                return timeId;
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }else{
                            //用户已经购买
                            return null;
                        }
                    }
                }else {
                    return null;
                }
            }else {
                return null;
            }
        }

        return null;
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (!hasKey) {
                //因为是stringRedisTemplate，所有返回类型为String
                List<String> collect = session.getRelationSkus().stream().map(
                        item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessions.stream().forEach(session -> {
            //1.随机码(防止攻击)
            String token = UUID.randomUUID().toString().replace("-", "");
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {
                    //缓存商品hash结
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //2.缓存秒杀商品信息(sku信息)
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData2("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(info);
                    }
                    //3.缓存秒杀商品基本信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);
                    //4.设置当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    redisTo.setRandomCode(token);
                    String jsonString = JSON.toJSONString(redisTo);
                    ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);
                    //如果当前场次的商品库存信息已经上架，就不需要再上架
                    //5.引入分布式信号量(限流)
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    //商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }
            });
        });
    }
}
