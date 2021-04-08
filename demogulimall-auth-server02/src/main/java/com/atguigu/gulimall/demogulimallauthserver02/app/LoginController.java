package com.atguigu.gulimall.demogulimallauthserver02.app;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.demogulimallauthserver02.feign.MemberFeignService;
import com.atguigu.gulimall.demogulimallauthserver02.feign.ThirdPartFeignService;
import com.atguigu.gulimall.demogulimallauthserver02.vo.UserLoginVo;
import com.atguigu.gulimall.demogulimallauthserver02.vo.UserRegistVo;
import com.sun.xml.internal.bind.v2.TODO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一个请求跳转到一个页面
     * SpringMVC viewcontroller 将请求和页面映射过来
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //TODO 接口防刷
        if(!StringUtils.isEmpty(phone)&phone !=null) {
            String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
            if (!StringUtils.isEmpty(redisCode)) {
                long time = Long.parseLong(redisCode.split("_")[1]);
                if (System.currentTimeMillis() - time < 60000) {
                    //60秒内不能再发
                    return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
                }
            }
            //2.验证码有效时间
            //String code1 = UUID.randomUUID().toString().substring(0, 5)+"_"+System.currentTimeMillis();
            String code1 = "123456";
            String code2 = "123456" + "_" + System.currentTimeMillis();
            //redis缓存验证码
            stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code2, 10, TimeUnit.MINUTES);
            //thirdPartFeignService.sendCode(phone,code1);
            return R.ok();
        }else {
            return R.error(BizCodeEnume.PHONE_NULL_EXCEPTION.getCode(),BizCodeEnume.PHONE_NULL_EXCEPTION.getMsg());
        }
    }

    //重定向携带数据RedirectAttributes
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            //如果校验出错,重定向到注册页(转发会重复提交表单)
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //1.校验验证码
        String code = vo.getCode();
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                //验证码通过
                //删除验证码;令牌机制
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //调用远程服务,注册
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    //注册成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData2("msg",new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    //前端传来k,v参数不需要加@RequestBody
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        //远程登录
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            MemberRespVo data = r.getData(new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String,String>  errors = new HashMap<>();
            errors.put("msg",r.getData2("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
