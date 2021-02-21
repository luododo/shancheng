package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimail.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimail.ware.service.PurchaseDetailService;
import com.atguigu.gulimail.ware.service.WareSkuService;
import com.atguigu.gulimail.ware.vo.MergeVo;
import com.atguigu.gulimail.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimail.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.ware.dao.PurchaseDao;
import com.atguigu.gulimail.ware.entity.PurchaseEntity;
import com.atguigu.gulimail.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;


    @Override
    public PageUtils queryUnreceivePage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> {
                w.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<Long> collect = items.stream().filter(i -> {
                    PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
                    if (purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode()
                            || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
                        return true;
                    } else {
                        return false;
                    }
                }
        ).collect(Collectors.toList());
        if (collect != null && collect.size() > 0) {
            //新建采购单的情况
            if (purchaseId == null) {
                PurchaseEntity purchaseEntity = new PurchaseEntity();
                purchaseEntity.setCreateTime(new Date());
                purchaseEntity.setUpdateTime(new Date());
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
                this.save(purchaseEntity);
                purchaseId = purchaseEntity.getId();
            }
            List<PurchaseDetailEntity> collect1 = collect.stream().map(i -> {
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
            //优化时间更新
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            //领取采购单最终是更新操作
            return this.updateById(purchaseEntity);
        } else {
            return false;
        }
    }


//    @Transactional
//    @Override
//    public boolean mergePurchase(MergeVo mergeVo) {
//        Long purchaseId = mergeVo.getPurchaseId();
//        List<Long> items = mergeVo.getItems();
//        Long finalPurchaseId = purchaseId;
//        //新建采购单的情况
//        if (purchaseId == null) {
//            PurchaseEntity purchaseEntity = new PurchaseEntity();
//            purchaseEntity.setCreateTime(new Date());
//            purchaseEntity.setUpdateTime(new Date());
//            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
//            this.save(purchaseEntity);
//            purchaseId = purchaseEntity.getId();
//        }
//        List<PurchaseDetailEntity> collect = items.stream().filter(i -> {
//            PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(i);
//            if (purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.CREATED.getCode()
//                    || purchaseDetailEntity.getStatus() == WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode()) {
//                return true;
//            } else {
//                return false;
//            }
//        }).map(i -> {
//            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
//            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
//            purchaseDetailEntity.setId(i);
//            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
//            return purchaseDetailEntity;
//        }).collect(Collectors.toList());
//        //领取采购单最终是更新操作
//        if (collect != null && collect.size() > 0) {
//            purchaseDetailService.updateBatchById(collect);
//            //优化时间更新
//            PurchaseEntity purchaseEntity = new PurchaseEntity();
//            purchaseEntity.setId(purchaseId);
//            purchaseEntity.setUpdateTime(new Date());
//            return this.updateById(purchaseEntity);
//        } else {
//            return false;
//        }
//    }

    @Override
    public void received(List<Long> ids) {
        //1.确认当前采购单状态
        List<PurchaseEntity> collect = ids.stream().map(item -> {
            PurchaseEntity purchaseEntity = this.getById(item);
            return purchaseEntity;
        }).filter(id -> {
            if (id.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    id.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());
        //2.改变采购单状态
        this.updateBatchById(collect);
        //3.改变采购项目状态
        if (collect != null && collect.size() > 0) {
            collect.forEach(item -> {
                List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
                List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                    PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                    purchaseDetailEntity.setId(entity.getId());
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return purchaseDetailEntity;
                }).collect(Collectors.toList());
                purchaseDetailService.updateBatchById(detailEntities);
            });
        }
    }

    @Override
    public void done(PurchaseDoneVo doneVo) {
        Long id = doneVo.getId();
        //1.改变采购项状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            } else {
                //2.将成功采购的进行入库
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            updates.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updates);
        //3.改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() :
                WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }
}