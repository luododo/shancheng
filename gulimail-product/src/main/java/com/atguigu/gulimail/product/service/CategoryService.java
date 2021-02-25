package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-28 02:51:14
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> longs);

    //找到catelogId完整路径
    Long[] findCatelogPath(Long catelogId);

    List<CategoryEntity> getLevel1();

    Map<String, List<Catelog2Vo>> getCatelogJson();
}

