package com.atguigu.gulimall.search.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping(value = "/product/attr/info/{attrId}",method = RequestMethod.GET)
    public R attrInfo(@PathVariable("attrId") Long attrId);
}
