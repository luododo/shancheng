package com.atguigu.gulimall.thirdpart.service;

import com.atguigu.gulimall.thirdpart.config.MyAccess;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface SendSms {
    public boolean send(MyAccess access,String phoneNum, String code);
}
