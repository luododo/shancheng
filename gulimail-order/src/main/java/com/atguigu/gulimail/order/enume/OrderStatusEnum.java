package com.atguigu.gulimail.order.enume;

public enum OrderStatusEnum {
    CREATE_NEW(0,""),
    PAYED(1,""),
    SENDED(2,""),
    RECIEVED(3,""),
    CANCLED(4,""),
    SERVICING(5,""),
    SERVICED(6,"");
    private Integer code;
    private String msg;

    OrderStatusEnum(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
