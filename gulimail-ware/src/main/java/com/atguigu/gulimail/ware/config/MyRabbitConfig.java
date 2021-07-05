package com.atguigu.gulimail.ware.config;

import com.atguigu.gulimail.ware.entity.WareInfoEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    /**
     * 使用JSON序列化机制,进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.release.#", null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked", null);
    }

    //第一次监听消息时，idea会连接rabbitMQ,此时才会创建rdbbitMQ中没有的队列、交换机和绑定关系
    //如果需要修改rabbitMQ中已存在的队列交换机,需要先删除，然后再次创建
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void listener(WareInfoEntity entity, Channel channel, Message msg) throws IOException {
//        System.out.println("收到过期的订单信息:准备关闭订单" + entity.getId());
//        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
//    }
}
