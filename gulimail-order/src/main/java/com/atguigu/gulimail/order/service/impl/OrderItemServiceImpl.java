package com.atguigu.gulimail.order.service.impl;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.order.dao.OrderItemDao;
import com.atguigu.gulimail.order.entity.OrderItemEntity;
import com.atguigu.gulimail.order.service.OrderItemService;


@Service("orderItemService")
//@RabbitListener(queues = {"hello-java-queue"})//queues 声明需要监听的所有队列
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

//    @RabbitHandler
//    public void reciveMessage(Message msg, OrderReturnReasonEntity content, Channel channel){
//        //消息头
//        MessageProperties properties = msg.getMessageProperties();
//        //消息体
//        byte[] body = msg.getBody();
//        //System.out.println("接收到消息...内容:"+msg+"==>类型"+content);
//        //System.out.println("消息头:"+properties);
//        //System.out.println("消息体:"+body);
//        //Channel内按顺序自增
//        long deliveryTag = msg.getMessageProperties().getDeliveryTag();
//        //签收消息,multiple是否批量签收消息;拒签消息,requeue=true发回服务器,服务器重新入队,false丢弃消息
//        try {
//            if(deliveryTag%2==0){
//                channel.basicAck(deliveryTag,false);
//                System.out.println("签收了消息..."+deliveryTag);
//            }else {
//             channel.basicNack(deliveryTag,false,false);
//                System.out.println("拒签了消息..."+deliveryTag);
//            }
//        }catch (Exception e){
//            //网络中断
//        }
//
//        System.out.println("接收到消息...内容:"+content);
//    }
//    @RabbitHandler
//    public void reciveMessage2(OrderEntity content){
//        System.out.println("接收到消息...内容:"+content);
//    }
}