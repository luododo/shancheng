package com.atguigu.gulimail.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, Model model){
        //查出用户所有订单列表数据
        Map<String,Object> page = new HashMap<>();
        page.put("page",pageNum.toString());
        //远程接口（需要先登录）,使用feign拦截器requestInterceptor获取cookies
        R r = orderFeignService.listWithItem(page);
//        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders",r);
        return "orderList";
    }
}
