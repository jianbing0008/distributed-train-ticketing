package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.*;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.enums.TrainTypeEnum;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.business.req.DailyTrainTicketQueryReq;
import com.jiawa.train.business.req.DailyTrainTicketSaveReq;
import com.jiawa.train.business.resp.DailyTrainTicketQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
/**
 * DailyTrainTicket服务类，负责处理与DailyTrainTicket相关的业务逻辑
 */
public class DailyTrainTicketService {

    @Autowired
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Autowired
    private TrainStationService trainStationService;
    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;



    /**
     * 保存DailyTrainTicket信息
     *
     * @param req DailyTrainTicket保存请求对象，包含DailyTrainTicket的基本信息
     */
    public void save(DailyTrainTicketSaveReq req){
        // 获取当前时间，用于记录DailyTrainTicket信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为DailyTrainTicket对象，便于后续操作
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增DailyTrainTicket
            // 设置DailyTrainTicket的会员ID，来源于登录会员上下文
            // 生成DailyTrainTicket的唯一ID
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            // 设置DailyTrainTicket信息的创建和更新时间为当前时间
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            // 插入DailyTrainTicket信息到数据库
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else{  // 不为空则更新DailyTrainTicket信息
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }

    }

    /**
     * 查询DailyTrainTicket列表
     *
     * @param req DailyTrainTicket查询请求对象，可能包含DailyTrainTicket的会员ID等查询条件
     */
    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req){
        // 创建DailyTrainTicket示例对象，用于构造查询条件
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        //根据id倒序排序
        dailyTrainTicketExample.setOrderByClause("id desc,train_code asc");
        // 创建查询条件对象
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();

        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }
        if(ObjectUtil.isNotEmpty(req.getStart())){
            criteria.andStartEqualTo(req.getStart());
        }
        if(ObjectUtil.isNotEmpty(req.getEnd())){
            criteria.andEndEqualTo(req.getEnd());
        }


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的DailyTrainTicket信息
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<DailyTrainTicket> pageInfo = new PageInfo<>(dailyTrainTicketList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成每日余票信息
     *
     * @param dailyTrain
     * @param date
     * @param trainCode
     */
    @Transactional
    public void genDaily(DailyTrain dailyTrain, Date date , String trainCode){
        log.info("生成日期【{}】车次【{}】的余票信息开始", DateUtil.formatDate(date), trainCode);
        // 删除之前数据
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);

        // 查询途径的车站信息
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainStationList)){
            log.info("该车次没有车站基础数据，生成该车次的余票信息结束");
            return;
        }

        DateTime now = DateTime.now();
        // 座位数量
        int ydz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YDZ.getCode());
        int edz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.EDZ.getCode());
        int rw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.RW.getCode());
        int yw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YW.getCode());
        //票价 = 历程之和 * 座位单价 * 车次类型系数
        for (int i = 0; i < trainStationList.size(); i++) {
            //得到出发站
            TrainStation start = trainStationList.get(i);
            BigDecimal sumKm = BigDecimal.ZERO;
            for (int j = i + 1; j < trainStationList.size(); j++){
                //得到到达站
                TrainStation end = trainStationList.get(j);
                sumKm = sumKm.add(end.getKm());
                log.info("查询【{}】车次，【{}】-【{}】的余票信息开始", trainCode, start.getName(), end.getName());

                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(start.getName());
                dailyTrainTicket.setStartPinyin(start.getNamePinyin());
                dailyTrainTicket.setStartTime(start.getOutTime());
                dailyTrainTicket.setStartIndex(start.getIndex());
                dailyTrainTicket.setEnd(end.getName());
                dailyTrainTicket.setEndPinyin(end.getNamePinyin());
                dailyTrainTicket.setEndTime(end.getInTime());
                dailyTrainTicket.setEndIndex(end.getIndex());

                String TrainType = dailyTrain.getType();
                //计算票价系数：TrainTypeEnum.priceRate
                BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, TrainType);

                BigDecimal ydzPrice = sumKm.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal edzPrice = sumKm.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rwPrice = sumKm.multiply(SeatTypeEnum.RW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ywPrice = sumKm.multiply(SeatTypeEnum.YW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP);

                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setYdzPrice(ydzPrice);
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setEdzPrice(edzPrice);
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setRwPrice(rwPrice);
                dailyTrainTicket.setYw(yw);
                dailyTrainTicket.setYwPrice(ywPrice);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                // 插入数据
                dailyTrainTicketMapper.insert(dailyTrainTicket);
                log.info("查询【{}】车次，【{}】-【{}】的余票信息结束", trainCode, start.getName(), end.getName());
            }
        }
    }

    /**
     * 按唯一键查询
     * @param date
     * @param trainCode
     * @param start
     * @param end
     * @return
     */
    public DailyTrainTicket selectByUnique(Date date, String trainCode,String start, String end) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andStartEqualTo(start)
                .andEndEqualTo(end);
        List<DailyTrainTicket> list = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        if(CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

}