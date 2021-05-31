package com.atguigu.gulimail.order.feign;

import com.atguigu.gulimail.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@EnableFeignClients("gulimall-cart")
public interface CartFeginService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
