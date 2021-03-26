package com.atguigu.gulimall.thirdpart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Component
@Data
public class MyAccess {
    private String accessKeyId;
    private String secret;
    private String templateCode;
    private String signName;
}
