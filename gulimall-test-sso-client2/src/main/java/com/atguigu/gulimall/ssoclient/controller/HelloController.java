package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {
    @Value("${sso.server.url}")
    String ssoServerUrl;
    /**
     * 无需登录即可访问
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }


    /**
     * 可以感知登录服务器登录成功返回
     * ssoserver登录成功返回就会带上token
     * @return
     */
    @GetMapping("/boss")
    public String emploees(Model model, HttpSession session,@RequestParam(value = "token",required = false) String token){
        if(!StringUtils.isEmpty(token)){
            //去sso服务器获取当前token真正对应的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser",body);
        }
        Object loginUser = session.getAttribute("loginUser");
        if(loginUser == null){
            //没有登录,跳转到登录服务器进行登录

            return "redirect:"+ssoServerUrl+"?redirect_url=http://client2.com:8085/boss";
        }else {
            List<String> emps = new ArrayList<String>();
            emps.add("Jack");
            emps.add("Tom");
            emps.add("Ada");
            model.addAttribute("emps", emps);
            return "list";
        }
    }
}
