package com.jiawa.train.business.service;

import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
/**
 * ConfirmOrder服务类，负责处理与ConfirmOrder相关的业务逻辑
 */
public class AfterConfirmOrderService {

    @Autowired
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    /**
     * 选中座位后事务处理：
     * 座位表修改售卖情况sell；
     * 余票详情表修改余票；
     * 为会员增加购票记录
     * 更新确认订单为成功
     * @param finalSeatList
     */
    @Transactional
    public void afterDoConfirm(List<DailyTrainSeat> finalSeatList){
        for (DailyTrainSeat dailyTrainSeat : finalSeatList) {
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(dailyTrainSeat.getId());
            seatForUpdate.setSell(dailyTrainSeat.getSell());
            seatForUpdate.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatForUpdate);
        }

    }


}
