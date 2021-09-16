package com.atguigu.gulimail.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimail.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimail.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        return list;
    }

    //计算最近三天的时间
    private LocalDateTime startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }

    private LocalDateTime endTime() {
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(2);
        LocalTime max = LocalTime.MIN;
        LocalDateTime end = LocalDateTime.of(localDate, max);
        end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return end;
    }
}