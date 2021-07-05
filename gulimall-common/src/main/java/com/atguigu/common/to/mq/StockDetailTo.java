package com.atguigu.common.to.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class StockDetailTo {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    private Long wareId;

    private Integer lockStatus;
}
