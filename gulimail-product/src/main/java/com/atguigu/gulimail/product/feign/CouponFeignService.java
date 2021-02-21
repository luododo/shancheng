package com.atguigu.gulimail.product.feign;


import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 远程步骤:
     * 1.@RequestBody将对象spuBoundTo转换为json
     * 2.找到gulimall-coupon服务,给/coupon/spubonds/save发送请求,
     * 将上一步转的json放在请求体位置,发送请求
     * 3.对方服务受到请求@RequestBody将请求体的json转为SpuBoundsEntity,
     * 只要json数据模型是兼容的(属性名一一对应),服务双方无需使用同一个to
     * @param spuBoundTo
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
