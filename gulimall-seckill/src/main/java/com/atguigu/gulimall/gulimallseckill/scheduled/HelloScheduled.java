package com.atguigu.gulimall.gulimallseckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
@EnableScheduling
public class HelloScheduled {

    @Async
    @Scheduled(cron = "0/3 * * * * ?")
    public void hello(){

        log.info("hello...");
    }
}
