package com.atguigu.gulimall.thirdpart.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdpart.config.MyAccess;
import com.atguigu.gulimall.thirdpart.service.SendSms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/sms")
public class SmsApiController {
    @Autowired
    private SendSms sendSms;

    @Autowired
    private MyAccess access;

    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code")String code){
        sendSms.send(access,phone,code);
        return R.ok();
    }


}
