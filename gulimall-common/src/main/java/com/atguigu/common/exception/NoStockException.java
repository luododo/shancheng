package com.atguigu.common.exception;

public class NoStockException extends RuntimeException{
    private Long skuId;
    public NoStockException(Long skuId){
        super(skuId+"号商品没有足够的库存");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
