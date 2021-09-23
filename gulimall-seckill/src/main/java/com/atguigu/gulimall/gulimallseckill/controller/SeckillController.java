package com.atguigu.gulimall.gulimallseckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.gulimallseckill.service.SeckillService;
import com.atguigu.gulimall.gulimallseckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 查询当前商品是否参与秒杀
     * @param skuId
     * @return
     */
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }


    @GetMapping("/kill")
    public R seckill(@RequestParam("killId") String killId,@RequestParam("key") String key,@RequestParam("num") Integer num){
        //判断是否登录
        String orderSn = seckillService.kill(killId,key,num);
        return R.ok().setData(orderSn);
    }
}
