package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.DailyTrainCarriage;
import com.jiawa.train.business.domain.DailyTrainCarriageExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.DailyTrainCarriageMapper;
import com.jiawa.train.business.req.DailyTrainCarriageQueryReq;
import com.jiawa.train.business.req.DailyTrainCarriageSaveReq;
import com.jiawa.train.business.resp.DailyTrainCarriageQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * DailyTrainCarriage服务类，负责处理与DailyTrainCarriage相关的业务逻辑
 */
public class DailyTrainCarriageService {

    @Autowired
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;

    /**
     * 保存DailyTrainCarriage信息
     *
     * @param req DailyTrainCarriage保存请求对象，包含DailyTrainCarriage的基本信息
     */
    public void save(DailyTrainCarriageSaveReq req){
        // 获取当前时间，用于记录DailyTrainCarriage信息的创建和更新时间
        DateTime now = DateTime.now();

        //自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(seatColEnums.size() * req.getRowCount());

        // 将请求对象转换为DailyTrainCarriage对象，便于后续操作
        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增DailyTrainCarriage
            // 设置DailyTrainCarriage的会员ID，来源于登录会员上下文
            // 生成DailyTrainCarriage的唯一ID
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            // 设置DailyTrainCarriage信息的创建和更新时间为当前时间
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            // 插入DailyTrainCarriage信息到数据库
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }else{  // 不为空则更新DailyTrainCarriage信息
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }

    }

    /**
     * 查询DailyTrainCarriage列表
     *
     * @param req DailyTrainCarriage查询请求对象，可能包含DailyTrainCarriage的会员ID等查询条件
     */
    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req){
        // 创建DailyTrainCarriage示例对象，用于构造查询条件
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        //根据id倒序排序
        dailyTrainCarriageExample.setOrderByClause("date desc, train_code asc, `index` asc");
        // 创建查询条件对象
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();

        if (ObjUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的DailyTrainCarriage信息
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriageList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<DailyTrainCarriageQueryResp> list = BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }
}