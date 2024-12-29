package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.domain.DailyTrainStationExample;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.mapper.DailyTrainStationMapper;
import com.jiawa.train.business.req.DailyTrainStationQueryReq;
import com.jiawa.train.business.req.DailyTrainStationSaveReq;
import com.jiawa.train.business.resp.DailyTrainStationQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
/**
 * DailyTrainStation服务类，负责处理与DailyTrainStation相关的业务逻辑
 */
public class DailyTrainStationService {

    @Autowired
    private DailyTrainStationMapper dailyTrainStationMapper;
    @Autowired
    private TrainStationService trainStationService;


    /**
     * 保存DailyTrainStation信息
     *
     * @param req DailyTrainStation保存请求对象，包含DailyTrainStation的基本信息
     */
    public void save(DailyTrainStationSaveReq req){
        // 获取当前时间，用于记录DailyTrainStation信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为DailyTrainStation对象，便于后续操作
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增DailyTrainStation
            // 设置DailyTrainStation的会员ID，来源于登录会员上下文
            // 生成DailyTrainStation的唯一ID
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            // 设置DailyTrainStation信息的创建和更新时间为当前时间
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            // 插入DailyTrainStation信息到数据库
            dailyTrainStationMapper.insert(dailyTrainStation);
        }else{  // 不为空则更新DailyTrainStation信息
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
        }

    }

    /**
     * 查询DailyTrainStation列表
     *
     * @param req DailyTrainStation查询请求对象，可能包含DailyTrainStation的会员ID等查询条件
     */
    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req){
        // 创建DailyTrainStation示例对象，用于构造查询条件
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        //根据id倒序排序
        dailyTrainStationExample.setOrderByClause("date desc, train_code asc, `index` asc");
        // 创建查询条件对象
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();

        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的DailyTrainStation信息
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }
    public long countByTrainCode(Date date, String trainCode) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
        return dailyTrainStationMapper.countByExample(dailyTrainStationExample);
    }


    /**
     * 生成某日车站信息
     * @param date
     * @param trainCode
     */
    @Transactional
    public void genDaily(Date date, String trainCode) {
        log.info("生成日期【{}】车次【{}】的车站信息开始", DateUtil.formatDate(date), trainCode);
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(trainCode);
        if(CollUtil.isEmpty(trainStationList)){
            log.info("该车次没有车站基础数据，生成该车次的车站信息结束");
            return;
        }
        for (TrainStation trainStation : trainStationList) {
            genDailyStation(date, trainCode, trainStation);
        }
        log.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date), trainCode);
    }

    /**
     * 生成车站信息
     * @param date
     * @param trainCode
     * @param trainStation
     */
    private void genDailyStation(Date date, String trainCode, TrainStation trainStation) {
        // 删除之前数据
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode)
                .andIndexEqualTo(trainStation.getIndex());
        dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);

        // 生成数据
        Date now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
        dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
        dailyTrainStation.setCreateTime(now);
        dailyTrainStation.setUpdateTime(now);
        dailyTrainStation.setDate(date);
        dailyTrainStationMapper.insert(dailyTrainStation);



    }
}