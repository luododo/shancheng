package com.atguigu.gulimall.demogulimallauthserver02.app;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.demogulimallauthserver02.feign.ThirdPartFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一个请求跳转到一个页面
     * SpringMVC viewcontroller 将请求和页面映射过来
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //1.接口防刷

        //2.验证码有效时间

        String code = UUID.randomUUID().toString().substring(0, 5);
        thirdPartFeignService.sendCode(phone,code);
        return R.ok();
    }
}
