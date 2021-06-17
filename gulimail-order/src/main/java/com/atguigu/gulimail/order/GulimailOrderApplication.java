package com.atguigu.gulimail.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@MapperScan("com.atguigu.gulimail.order.dao")
@EnableRabbit
@SpringBootApplication
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atguigu.gulimail.order.feign")
@EnableDiscoveryClient
public class GulimailOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimailOrderApplication.class, args);
	}

}
