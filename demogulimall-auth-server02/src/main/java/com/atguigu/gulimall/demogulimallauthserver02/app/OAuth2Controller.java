package com.atguigu.gulimall.demogulimallauthserver02.app;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.demogulimallauthserver02.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

//社交登录
public class OAuth2Controller {

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {
        Map<String,String> header = new HashMap<>();
        Map<String,String> query = new HashMap<>();
        Map<String,String> map = new HashMap<>();
        map.put("client_id","1738071962");
        map.put("secret","177762fff54da4094edf8c690d9d8f27");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code",code);
        //根据code换取access token
        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2.0/access_token", "post", null, null, map);
        if(response.getStatusLine().getStatusCode()==200){
            String json = EntityUtils.toString(response.getEntity());
            JSON.parseObject(json, SocialUser.class);
            //知道当前是哪个社交用户
            //1.如果是第一次登录,自动注册(为当前社交用户生成一个当前会员信息账号）
            //登录或者注册这个社交用户
        }else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
        //登录成功调回首页

        return "redirect:http://gulimall.com";
    }
}
