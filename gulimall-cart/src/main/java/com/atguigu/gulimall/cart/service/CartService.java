package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    //将商品添加到购物车
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;
    //查询购物车的某个购物项
    CartItem getCartItem(Long skuId);
    //清空购物车
    void clearCart(String cartKey);

    Cart getCart() throws ExecutionException, InterruptedException;
    //勾选购物项
    void checkItem(Long skuId, Integer check);
    //修改购物项数量
    void countItem(Long skuId, Integer num);
    //删除购物项
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
