package com.atguigu.gulimail.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


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

    @Setter @Getter
    Map<Long,Boolean> stocks;

    //唯一令牌,多次提交订单防重（如网络问题用户多次提交订单)
    @Getter @Setter
    String token;

    public Integer getCount() {
        Integer i = 0;
        if(items!=null){
            for (OrderItemVo item : items){
                i+=item.getCount();
            }
        }
        return i;
    }

    //总价
    //BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    //应付价格
    //BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }
}
