package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainSeatExample;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.req.DailyTrainSeatQueryReq;
import com.jiawa.train.business.req.DailyTrainSeatSaveReq;
import com.jiawa.train.business.resp.DailyTrainSeatQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * DailyTrainSeat服务类，负责处理与DailyTrainSeat相关的业务逻辑
 */
public class DailyTrainSeatService {

    @Autowired
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    /**
     * 保存DailyTrainSeat信息
     *
     * @param req DailyTrainSeat保存请求对象，包含DailyTrainSeat的基本信息
     */
    public void save(DailyTrainSeatSaveReq req){
        // 获取当前时间，用于记录DailyTrainSeat信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为DailyTrainSeat对象，便于后续操作
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增DailyTrainSeat
            // 设置DailyTrainSeat的会员ID，来源于登录会员上下文
            // 生成DailyTrainSeat的唯一ID
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            // 设置DailyTrainSeat信息的创建和更新时间为当前时间
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            // 插入DailyTrainSeat信息到数据库
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }else{  // 不为空则更新DailyTrainSeat信息
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }

    }

    /**
     * 查询DailyTrainSeat列表
     *
     * @param req DailyTrainSeat查询请求对象，可能包含DailyTrainSeat的会员ID等查询条件
     */
    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req){
        // 创建DailyTrainSeat示例对象，用于构造查询条件
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        //根据id倒序排序
        dailyTrainSeatExample.setOrderByClause("train_code asc, carriage_index asc, carriage_seat_index asc");
        // 创建查询条件对象
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的DailyTrainSeat信息
        List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<DailyTrainSeatQueryResp> list = BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainSeatMapper.deleteByPrimaryKey(id);
    }
}