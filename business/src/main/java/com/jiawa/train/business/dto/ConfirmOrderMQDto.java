package com.jiawa.train.business.dto;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class ConfirmOrderMQDto {

    /**
     *  日志流水号
     */
    private Object logId;

    /**
     *  日期
     */
    private Date date;

    /**
     *  车次编号
     */
    private String trainCode;



}