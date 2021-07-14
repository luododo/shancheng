package com.atguigu.gulimail.order.listener;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RestController
public class OrderPayedListener {

    @RequestMapping("/payed/notify")
    public String handleAlipayed(HttpServletRequest request){
        Map<String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知:"+map);
        return "success";
    }
}
