package com.jiawa.train.common.exception;

//注意不能在枚举类上使用lombok的Data注解来自动生成getter/setter
public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已注册"),  //注意枚举常量是以逗号为分隔的
    MEMBER_MOBILE_NOT_EXIST("请先获取短信验证码"),
    MEMBER_MOBILE_CODE_ERROR("短信验证码错误"),

    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站名以存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次编号已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("同车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("同车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("同车次厢号已存在"),

    BUSINESS_CONFIRM_ORDER_TICKET_NUM_ERROR("余票不足"),
    BUSINESS_CONFIRM_ORDER_ERROR("服务器忙，请稍后重试"),
    BUSINESS_CONFIRM_LOCK_ERROR("当前抢票人数过多，请稍后重试"),
    CONFIRM_ORDER_FLOW_EXCEPTION("当前抢票人数太多了，请稍后重试"),
    CONFIRM_ORDER_SK_TOKEN_FAIL("当前抢票人数过多，请5秒后重试"),
    CONFIRM_ORDER_TICKET_COUNT_ERROR("余票不足");

    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                '}';
    }
}