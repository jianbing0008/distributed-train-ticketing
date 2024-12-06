package com.jiawa.train.business.req;

import com.jiawa.train.common.req.PageReq;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class DailyTrainStationQueryReq extends PageReq {

    //@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8") 注意：post请求可以用这个，但是get请求不行
    @DateTimeFormat(pattern = "yyyy-MM-dd") // get请求可以用这个
    private Date date;

    private String TrainCode;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTrainCode() {
        return TrainCode;
    }

    public void setTrainCode(String trainCode) {
        this.TrainCode = trainCode;
    }

    @Override
    public String toString() {
        return "DailyTrainStationQueryReq{" +
                "date=" + date +
                ", TrainCode='" + TrainCode + '\'' +
                "} " + super.toString();
    }
}