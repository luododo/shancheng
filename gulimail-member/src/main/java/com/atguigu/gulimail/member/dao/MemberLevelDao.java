package com.atguigu.gulimail.member.dao;

import com.atguigu.gulimail.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author AdverseQ
 * @email sunlightcs@gmail.com
 * @date 2020-11-30 06:19:10
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
