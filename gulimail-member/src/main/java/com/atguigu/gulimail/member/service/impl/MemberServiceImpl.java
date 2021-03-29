package com.atguigu.gulimail.member.service.impl;

import com.atguigu.gulimail.member.dao.MemberLevelDao;
import com.atguigu.gulimail.member.entity.MemberLevelEntity;
import com.atguigu.gulimail.member.exception.PhoneExistException;
import com.atguigu.gulimail.member.exception.UserNameExistException;
import com.atguigu.gulimail.member.vo.MemberLoginVo;
import com.atguigu.gulimail.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.member.dao.MemberDao;
import com.atguigu.gulimail.member.entity.MemberEntity;
import com.atguigu.gulimail.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao dao = this.baseMapper;
        MemberEntity entity = new MemberEntity();
        //设置默认用户等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());
        //检查数据是否唯一,为了让controller感知异常,异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        //密码加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);
        dao.insert(entity);
        //其它默认信息
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao dao = this.baseMapper;
        Integer mobile = dao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameExistException {
        MemberDao dao = this.baseMapper;
        Integer cout = dao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (cout > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        //1.数据库查询
        MemberDao dao = this.baseMapper;
        MemberEntity entity = dao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if(entity == null){
            return null;
        }else {
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if(matches){
                return entity;
            }else {
                return null;
            }
        }
    }

}