package com.jiawa.train.common.util;

import cn.hutool.core.util.IdUtil;

/**
 * 封装hutool雪花算法
 * */
public class SnowUtil {
    private static long workId = 1;         //机器Id
    private static long dataCenterId = 1;   //数据中心

    public static long getSnowflakeNextId() {
        return IdUtil.getSnowflake(workId, dataCenterId).nextId();
    }

    public static String getSnowflakeNextStr() {
        return IdUtil.getSnowflake(workId, dataCenterId).nextIdStr();
    }
}