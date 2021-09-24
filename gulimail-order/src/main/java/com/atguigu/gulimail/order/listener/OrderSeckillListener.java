package com.atguigu.gulimail.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.order.queue")
@Slf4j
@Component
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo orderTo, Channel channel, Message msg) throws IOException {

        try {
            log.info("准备创建秒杀单的详细信息");
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.out.println("订单关闭异常，库存解锁异常" + e.getMessage());
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
