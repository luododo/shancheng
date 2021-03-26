package com.atguigu.gulimall.thirdpart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gulimall.thirdpart.config.MyAccess;
import com.atguigu.gulimall.thirdpart.service.SendSms;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SendSmsImpl implements SendSms {
    @Override
    public boolean send(MyAccess access,String phoneNum,String code) {
        //连接阿里云
        DefaultProfile profile = DefaultProfile.getProfile("cn-qingdao", access.getAccessKeyId(), access.getSecret());
        IAcsClient client = new DefaultAcsClient(profile);
        //构建请求
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        //自定义参数(手机号,验证码,签名,模板)
        request.putQueryParameter("PhoneNumbers", phoneNum);
        request.putQueryParameter("SignName", access.getSignName());
        request.putQueryParameter("TemplateCode", access.getTemplateCode());
        //验证码
        request.putQueryParameter("TemplateParam",JSONObject.toJSONString(code));
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
