package com.atguigu.gulimall.gulimallseckill.service;

import com.atguigu.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SeckillService {
    void upSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
