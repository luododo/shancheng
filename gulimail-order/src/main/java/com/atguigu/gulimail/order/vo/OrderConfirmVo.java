package com.atguigu.gulimail.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


public class OrderConfirmVo {
    @Setter @Getter
    List<MemberAddressVo> address;
    @Setter @Getter
    List<OrderItemVo> items;

    //发票记录...

    //优惠券信息...

    //积分信息
    @Setter @Getter
    Integer integration;

    //总价
    BigDecimal total;


    //应付价格
    BigDecimal payPrice;
}
