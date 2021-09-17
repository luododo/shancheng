package com.atguigu.gulimall.gulimallseckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallseckill.feign.CouponFeignService;
import com.atguigu.gulimall.gulimallseckill.feign.ProductFeignService;
import com.atguigu.gulimall.gulimallseckill.service.SeckillService;
import com.atguigu.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.gulimallseckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.gulimallseckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService{

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码

    @Override
    public void upSeckillSkuLatest3Days() {
        //1.扫描需要参与秒杀的活动
        R session = couponFeignService.getLates3DaySession();
        if(session.getCode() == 0){
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

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions){
        sessions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX+startTime+"_"+endTime;
            //因为是stringRedisTemplate，所有返回类型为String
            List<String> collect = session.getRelationSkus().stream().map(item -> item.getId().toString()).collect(Collectors.toList());
            stringRedisTemplate.opsForList().leftPushAll(key,collect);
        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions){
        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessions.stream().forEach(session ->{
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                //缓存商品hash结
                SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                //1.缓存基本数据(sku信息)
                R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                if(skuInfo.getCode() == 0){
                    SkuInfoVo info = skuInfo.getData2("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    redisTo.setSkuInfoVo(info);
                }
                //2.缓存秒杀信息
                BeanUtils.copyProperties(seckillSkuVo,redisTo);
                //3.设置当前商品的秒杀时间信息
                redisTo.setStartTime(session.getStartTime().getTime());
                redisTo.setEndTime(session.getEndTime().getTime());
                //4.随机码(防止攻击)
                String token = UUID.randomUUID().toString().replace("-", "");
                redisTo.setRandomCode(token);
                //5.引入分布式信号量(限流)
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                //商品可以秒杀的数量作为信号量
                semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                String jsonString = JSON.toJSONString(redisTo);
                ops.put(seckillSkuVo.getId().toString(),jsonString);
            });
        });
    }
}
