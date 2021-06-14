package com.atguigu.gulimail.ware;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.gulimail.ware.service.impl.WareSkuServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GulimailWareApplicationTests {

    @Autowired
    private WareSkuServiceImpl wareSkuService;

    @Test
    void contextLoads() {
        List<Long> list = new ArrayList<>();
        list.add(0,1L);
        list.add(1,2L);
        list.add(2,7L);
        List<SkuHasStockVo> stock = wareSkuService.getSkuHasStock(list);
        System.out.println(stock.size());
        for (int i = 0 ;i<3;i++){
            System.out.println(stock.get(i));
        }
    }

}
