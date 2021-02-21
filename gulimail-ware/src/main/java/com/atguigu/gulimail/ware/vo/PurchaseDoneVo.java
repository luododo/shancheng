package com.atguigu.gulimail.ware.vo;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class PurchaseDoneVo {
    @NonNull
    private Long id;

    private List<PurchaseItemDoneVo> items;

    public PurchaseDoneVo(){}
}
