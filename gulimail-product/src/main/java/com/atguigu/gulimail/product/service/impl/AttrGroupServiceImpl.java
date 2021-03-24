package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrService;
import com.atguigu.gulimail.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimail.product.vo.skuItemvo.SkuItemVo;
import com.atguigu.gulimail.product.vo.skuItemvo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.AttrGroupDao;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    new QueryWrapper<AttrGroupEntity>());
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> list = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupWithAttrsVo> collect = list.stream().map(item -> {
            //1查出当前分类下的所有属性分组
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);//复制属性(两个实体类中属性名必须一致)
            //2查出每个属性分组下的所有属性
            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            if (attrs != null) {
                attrGroupWithAttrsVo.setAttrs(attrs);
            }
            return attrGroupWithAttrsVo;
        }).filter(atttrVo -> {
            return atttrVo.getAttrs() != null && atttrVo.getAttrs().size() > 0;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        //1.查出当前spu对应的所有属性分组信息以及当前分组下所有属性对应的值
        //1.1当前spu有多少属性分组
        AttrGroupDao baseMapper = this.getBaseMapper();
        List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        return vos;
    }


//    @Override
//    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
//        List<AttrGroupEntity> list = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
//        List<AttrGroupWithAttrsVo> collect = list.stream().map(item -> {
//            //1查出当前分类下的所有属性分组
//            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
//            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);//复制属性(两个实体类中属性名必须一致)
//            //2查出每个属性分组下的所有属性
//            List<AttrEntity> attrs = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
//            if (attrs != null) {
//                attrGroupWithAttrsVo.setAttrs(attrs);
//                return attrGroupWithAttrsVo;
//            } else {
//                return null;
//            }
//            }).collect(Collectors.toList());
//        return removeNull(collect);
//    }
//
//    public static <T> List<T> removeNull(List<? extends T> oldList) {
//        oldList.removeAll(Collections.singleton(null));
//        return (List<T>) oldList;
//    }
}