package com.atguigu.gulimall.gulimallseckill.scheduled;

import com.atguigu.gulimall.gulimallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品上架,
 * 每晚3点上架未来三天的商品(可以提前预告),
 * 每天00:00:00 - 23:59:59
 */
@Slf4j
@Service
@EnableScheduling
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String unload_lock = "seckill:upload:lock";

    @Scheduled(cron = "0 * * * * ?")
    public void upSeckillSkuLatest3Days(){
        //1.重复上架无需处理
        log.info("上架秒杀商品信息");
        //2.幂等性，分布式锁
        RLock lock = redissonClient.getLock(unload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.upSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }
}
