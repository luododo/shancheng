package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimail.product.vo.skuItemvo.SkuItemSaleAttrsVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-28 02:51:14
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId")Long skuId);
}
