package com.atguigu.gulimail.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;//应付价格,验价
    private String note;//备注信息
    //用户相关信息从session中取出
}
