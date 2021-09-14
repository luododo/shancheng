package com.atguigu.gulimall.search.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping(value = "/product/attr/info/{attrId}",method = RequestMethod.GET)
    R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);
}
