package com.atguigu.gulimail.ware.listener;

import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimail.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到接收解锁库存的信息");
        try {
            wareSkuService.unlockStock(to);
            //不批量处理
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            System.out.println("rabbitMQ错误"+e.getMessage());
            //将消息重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
