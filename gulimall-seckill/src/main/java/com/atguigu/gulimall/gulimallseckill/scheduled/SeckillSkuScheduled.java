package com.atguigu.gulimall.gulimallseckill.scheduled;

import com.atguigu.gulimall.gulimallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 秒杀商品上架,
 * 每晚3点上架未来三天的商品(可以提前预告),
 * 每天00:00:00 - 23:59:59
 */
@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void upSeckillSkuLatest3Days(){
        //1.重复上架无需处理
        seckillService.upSeckillSkuLatest3Days();
    }
}
