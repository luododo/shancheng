package com.atguigu.gulimall.demogulimallauthserver02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession//整合redis存储session
@EnableFeignClients(basePackages = "com.atguigu.gulimall.demogulimallauthserver02.feign")
public class DemogulimallAuthServer02Application {

	public static void main(String[] args) {
		SpringApplication.run(DemogulimallAuthServer02Application.class, args);
	}

}
