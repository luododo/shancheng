package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.entity.SkuImagesEntity;
import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimail.product.service.*;
import com.atguigu.gulimail.product.vo.skuItemvo.SkuItemSaleAttrsVo;
import com.atguigu.gulimail.product.vo.skuItemvo.SkuItemVo;
import com.atguigu.gulimail.product.vo.skuItemvo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.SkuInfoDao;
import com.atguigu.gulimail.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByParams(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
            }
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils((page));
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1.sku基本信息获取 pms_sku_info
        SkuInfoEntity info = getById(skuId);
        skuItemVo.setInfo(info);
        Long spuId = info.getSpuId();
        Long catalogId = info.getCatalogId();
        //2.sku图片信息 pms_sku_images
        List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
        skuItemVo.setImagesEntites(images);
        //3.spu的销售属性组合
        List<SkuItemSaleAttrsVo> saleAttrsVos  = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(saleAttrsVos);
        skuItemVo.setSaleAttr(saleAttrsVos);
        //4.spu的详细介绍
        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
        skuItemVo.setDesp(spuInfoDescEntity);
        //5.规格参数
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        skuItemVo.setGroupAttrs(attrGroupVos);
        return skuItemVo;
    }
}