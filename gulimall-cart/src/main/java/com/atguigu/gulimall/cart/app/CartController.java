package com.atguigu.gulimall.cart.app;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ExecutionException;


@Controller
public class CartController {

    @Autowired
    CartService cartService;


    /**
     * 浏览器有一个cookies,user-key:标识用户身份,过期时间一个月。
     * 浏览器以后保存,每次访问都会带上这个cookie
     * 第一次登录需要创建一个临时用户
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        return "cartList";
    }

    /*
    添加商品到购物车
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId")Long skuId, @RequestParam("num") Integer num, Model model) throws ExecutionException, InterruptedException {
        CartItem cartItem = cartService.addToCart(skuId,num);
        model.addAttribute("item",cartItem);
        return "success";
    }
}
