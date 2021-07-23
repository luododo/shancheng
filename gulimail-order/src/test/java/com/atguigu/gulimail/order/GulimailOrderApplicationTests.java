package com.atguigu.gulimail.order;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
@Slf4j
class GulimailOrderApplicationTests {

	@Autowired
	AmqpAdmin amqpAdmin;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Test
	void sendMessageTest(){
		OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
		entity.setId(1L);
		entity.setCreateTime(new Date());
		//1.发送消息,如果发送对象使用序列化机制,将对象写出去,对象必须实现Serializable
		String s = "Hello World";
		//2.发送的消息变为json格式
		for (int i = 0;i<10;i++) {
			if(i%2==0) {
				entity.setName("Vc" + i);
				rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity);
			}else {
				OrderEntity orderEntity = new OrderEntity();
				orderEntity.setOrderSn(UUID.randomUUID().toString());
				rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderEntity);
			}
			log.info("消息发送完成{}" + entity);
		}
	}

	@Test
	void createExchange() {
		DirectExchange directExchange = new DirectExchange("hello-java-exchange",true,false);
		amqpAdmin.declareExchange(directExchange);
		log.info("exchange:[{}]创建成功","hello-java-exchange");
	}

	@Test
	void createQueue() {
		Queue queue = new Queue("hello-java-queue",true,false,false);
		amqpAdmin.declareQueue(queue);
		log.info("queue:[{}]创建成功","hello-java-queue");
	}

	@Test
	void createBinding(){
		Binding binding = new Binding("hello-java-queue",Binding.DestinationType.QUEUE,
				"hello-java-exchange",
				"hello.java",null);
		amqpAdmin.declareBinding(binding);
		log.info("binding:[{}]创建成功","hello-java-binding");
	}

	@Test
	void regionCodeTest(){
		String code = "110101";
		String code1 = code.substring(0,2);
		System.out.println(code1);
	}
}
