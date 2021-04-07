package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/userInfo")
    @ResponseBody
    public String userInfo(@RequestParam("token") String token){
        String s = redisTemplate.opsForValue().get(token);
        return s;
    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,@CookieValue(value = "sso_token",required = false) String sso_token) {
       //判断是否登录过？依据是否拥有cookie sso_token,如果有直接返回之前的页面
        if(!StringUtils.isEmpty(sso_token)){
           return "redirect:" + url+"?token="+sso_token;
       }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username, @RequestParam("password") String password,
                          @RequestParam("url") String url, HttpServletResponse response) {
        //登录成功跳转,跳回之前的页面
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //保存登录成功的用户,例如redis
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid,username,30, TimeUnit.MINUTES);
            Cookie sso_token = new Cookie("sso_token",uuid);
            response.addCookie(sso_token);
            return "redirect:" + url+"?token="+uuid;
        }
        return "login";
    }
}
