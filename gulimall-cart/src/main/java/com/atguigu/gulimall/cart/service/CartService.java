package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

public interface CartService {
    //将商品添加到购物车
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
    //查询购物车的某个购物项
    CartItem getCartItem(Long skuId);
}
