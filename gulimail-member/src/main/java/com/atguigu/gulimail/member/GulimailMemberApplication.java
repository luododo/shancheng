package com.atguigu.gulimail.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableFeignClients(basePackages = "com.atguigu.gulimail.member.feign")
@EnableRedisHttpSession
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimail.member.dao")
@SpringBootApplication
public class GulimailMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimailMemberApplication.class, args);
	}

}
