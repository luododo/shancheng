package com.atguigu.gulimail.ware.dao;

import com.atguigu.gulimail.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-30 07:21:00
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("skuNum") Integer skuNum);

    Long getSkuStock(Long skuId);

    List<Long> listWareIdHasSkuStock(Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);

    void unlockStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("num") Integer num);
}
