package com.atguigu.gulimail.coupon.dao;

import com.atguigu.gulimail.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-30 05:47:23
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
