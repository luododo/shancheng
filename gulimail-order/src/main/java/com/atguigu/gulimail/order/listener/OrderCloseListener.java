package com.atguigu.gulimail.order.listener;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message msg) throws IOException {
        try {
            System.out.println("收到过期的订单信息:准备关闭订单" + entity.getOrderSn());
            orderService.closeOrder(entity);
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            System.out.println("订单关闭异常，库存解锁异常" + e.getMessage());
            channel.basicReject(msg.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
