package com.atguigu.gulimail.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.atguigu.gulimail.order.dao")
@EnableRabbit
@SpringBootApplication
public class GulimailOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimailOrderApplication.class, args);
	}

}
