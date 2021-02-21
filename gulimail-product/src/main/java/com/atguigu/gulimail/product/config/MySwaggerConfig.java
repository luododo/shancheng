package com.atguigu.gulimail.product.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class MySwaggerConfig {

    //配置多个分组,多个docket实例即可
    @Bean
    public Docket docket1() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("A");
    }
    @Bean
    public Docket docket2() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("B");
    }
    @Bean
    public Docket docket3() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("C");
    }

    @Bean
    public Docket docket(Environment environment){
        //设置要开启swagger的环境
        //Profiles profiles = Profiles.of("dev","test");
        //通过environment.acceptsProfiles判断是否处在环境中
        //boolean flag = environment.acceptsProfiles(profiles);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("zy")
                //.enable(flag)//是否启用swagger
                .select()
                //RequestHandlerSelectors,配置要扫描接口的方式
                //basePackage:指定要扫描的包
                //any():扫描全部
                //none():不扫描
                //withClassAnnotation:扫描类上的注解,参数是一个注解的反射对象
                //withMethodAnnotation:扫描方法上的注解
                //.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .apis(RequestHandlerSelectors.basePackage("com.atguigu.gulimail.product.app"))
                .paths(PathSelectors.ant("/abc/**"))
                //paths(),过滤的路径
                .build();//工厂模式
    }

    //配置swagger信息,apiInfo
    private ApiInfo apiInfo(){
        //作者信息
        Contact contact =new Contact("zy", "http://www.apache.org/licenses/LICENSE-2.0", "zouyu_ds@126.com");
        return new ApiInfo(
                "zy的swagger API文档", "如果这世上存在邪恶的话，那就是人的心",
                "1.0", "urn:tos", contact,
                "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }
}
