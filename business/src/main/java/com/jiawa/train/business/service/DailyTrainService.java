package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.DailyTrain;
import com.jiawa.train.business.domain.DailyTrainExample;
import com.jiawa.train.business.domain.Train;
import com.jiawa.train.business.mapper.DailyTrainMapper;
import com.jiawa.train.business.req.DailyTrainQueryReq;
import com.jiawa.train.business.req.DailyTrainSaveReq;
import com.jiawa.train.business.resp.DailyTrainQueryResp;
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
 * DailyTrain服务类，负责处理与DailyTrain相关的业务逻辑
 */
public class DailyTrainService {

    @Autowired
    private DailyTrainMapper dailyTrainMapper;
    @Autowired
    private TrainService trainService;
    @Autowired
    private DailyTrainStationService dailyTrainStationService;
    @Autowired
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Autowired
    private DailyTrainSeatService dailyTrainSeatService;
    @Autowired
    private DailyTrainTicketService dailyTrainTicketService;
    @Autowired
    private SkTokenService skTokenService;

    /**
     * 保存DailyTrain信息
     *
     * @param req DailyTrain保存请求对象，包含DailyTrain的基本信息
     */
    public void save(DailyTrainSaveReq req){
        // 获取当前时间，用于记录DailyTrain信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为DailyTrain对象，便于后续操作
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增DailyTrain
            // 设置DailyTrain的会员ID，来源于登录会员上下文
            // 生成DailyTrain的唯一ID
            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
            // 设置DailyTrain信息的创建和更新时间为当前时间
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            // 插入DailyTrain信息到数据库
            dailyTrainMapper.insert(dailyTrain);
        }else{  // 不为空则更新DailyTrain信息
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
        }

    }

    /**
     * 查询DailyTrain列表
     *
     * @param req DailyTrain查询请求对象
     */
    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req){
        // 创建DailyTrain示例对象，用于构造查询条件
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        //根据code倒序排序
        dailyTrainExample.setOrderByClause("date desc, code asc");
        // 创建查询条件对象
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();

        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }

        if(ObjectUtil.isNotEmpty(req.getCode())){
            criteria.andCodeEqualTo(req.getCode());
        }




        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的DailyTrain信息
        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrainList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<DailyTrainQueryResp> list = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    /**
     * 生成某日所有车次信息，包括：车次、车站、车厢、座位
     * @param date
     */
    public void genDaily(Date date) {
        log.info("生成日期【{}】的train数据", date);
        List<Train> trainList = trainService.selectAll();
        if(CollUtil.isEmpty(trainList)){
            //一般获取列表都做判空逻辑
            log.info("没有车次数据，不生成train数据");
            return;
        }
        for (Train train : trainList) {
            genDailyTrain(date, train);
        }
        log.info("生成日期【{}】的train数据结束", date);
    }

    /**
     * 生成某日某车次的数据
     * @param date
     * @param train
     */
    @Transactional
    public void genDailyTrain(Date date, Train train) {
        log.info("生成日期【{}】车次【{}】的数据开始", DateUtil.formatDate(date), train.getCode());
        //生成前进行判断是否生成过数据了  在循环体进行判断  高内聚低耦合  删除已有当天数据
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.createCriteria().
                andDateEqualTo(date).
                andCodeEqualTo(train.getCode());
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        // 生成该车次的数据
        Date now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);
        log.info("生成日期【{}】车次【{}】的数据结束", DateUtil.formatDate(date), train.getCode());


        // 生成该车次的车站数据
        dailyTrainStationService.genDaily(date, train.getCode());

        // 生成该车次的车厢数据
        dailyTrainCarriageService.genDaily(date, train.getCode());

        // 生成该车次的座位数据
        dailyTrainSeatService.genDaily(date, train.getCode());

        // 生成该车次的余票数据
        dailyTrainTicketService.genDaily(dailyTrain, date, train.getCode());

        // 生成令牌余量数据
        skTokenService.genDaily(date, train.getCode());

    }
}