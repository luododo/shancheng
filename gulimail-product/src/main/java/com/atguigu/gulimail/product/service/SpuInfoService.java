package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimail.product.vo.spvsavevo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-28 02:51:14
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    PageUtils queryPageByParams(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

