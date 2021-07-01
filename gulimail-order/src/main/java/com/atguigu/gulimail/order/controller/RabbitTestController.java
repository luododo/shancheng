package com.atguigu.gulimail.order.controller;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitTestController {

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @GetMapping("/sendMQ")
//    public String sendMQ(@RequestParam(value = "num", defaultValue = "10") Integer num) {
//        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
//        entity.setId(1L);
//        entity.setCreateTime(new Date());
//        //1.发送消息,如果发送对象使用序列化机制,将对象写出去,对象必须实现Serializable
//        String s = "Hello World";
//        //2.发送的消息变为json格式
//        for (int i = 0; i < num; i++) {
//            if (i % 2 == 0) {
//                entity.setName("Vc" + i);
//                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity
//                        , new CorrelationData(UUID.randomUUID().toString()));
//            } else {
//                OrderEntity orderEntity = new OrderEntity();
//                orderEntity.setOrderSn(UUID.randomUUID().toString());
//                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity,
//                        new CorrelationData(UUID.randomUUID().toString()));
//            }
//        }
//        return "ok";
//    }
}
