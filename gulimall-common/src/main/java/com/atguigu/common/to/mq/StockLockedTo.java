package com.atguigu.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo {

    private Long id;//库存工作单Id

    private StockDetailTo detail;//工作单详情的所有Id
}
