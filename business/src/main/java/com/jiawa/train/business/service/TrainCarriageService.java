package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainCarriageExample;
import com.jiawa.train.business.enums.SeatColEnum;
import com.jiawa.train.business.mapper.TrainCarriageMapper;
import com.jiawa.train.business.req.TrainCarriageQueryReq;
import com.jiawa.train.business.req.TrainCarriageSaveReq;
import com.jiawa.train.business.resp.TrainCarriageQueryResp;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * TrainCarriage服务类，负责处理与TrainCarriage相关的业务逻辑
 */
public class TrainCarriageService {

    @Autowired
    private TrainCarriageMapper trainCarriageMapper;


    /**
     * 保存TrainCarriage信息
     *
     * @param req TrainCarriage保存请求对象，包含TrainCarriage的基本信息
     */
    public void save(TrainCarriageSaveReq req){
        // 获取当前时间，用于记录TrainCarriage信息的创建和更新时间
        DateTime now = DateTime.now();

        //自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(seatColEnums.size() * req.getRowCount());


        // 将请求对象转换为TrainCarriage对象，便于后续操作
        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增TrainCarriage
            // 保存之前，先校验唯一键是否存在
            TrainCarriage trainCarriageDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if(ObjectUtil.isNotEmpty(trainCarriageDB)){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }
            // 设置TrainCarriage的会员ID，来源于登录会员上下文
            // 生成TrainCarriage的唯一ID
            trainCarriage.setId(SnowUtil.getSnowflakeNextId());
            // 设置TrainCarriage信息的创建和更新时间为当前时间
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            // 插入TrainCarriage信息到数据库
            trainCarriageMapper.insert(trainCarriage);
        }else{  // 不为空则更新TrainCarriage信息
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.updateByPrimaryKey(trainCarriage);
        }

    }

    private TrainCarriage selectByUnique(String trainCode, Integer index) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainCarriage> list = trainCarriageMapper.selectByExample(trainCarriageExample);
        if(CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询TrainCarriage列表
     *
     * @param req TrainCarriage查询请求对象，可能包含TrainCarriage的会员ID等查询条件
     */
    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req){
        // 创建TrainCarriage示例对象，用于构造查询条件
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        //根据id倒序排序
        trainCarriageExample.setOrderByClause("train_code asc, `index` asc");
        // 创建查询条件对象
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的TrainCarriage信息
        List<TrainCarriage> trainCarriageList = trainCarriageMapper.selectByExample(trainCarriageExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<TrainCarriage> pageInfo = new PageInfo<>(trainCarriageList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<TrainCarriageQueryResp> list = BeanUtil.copyToList(trainCarriageList, TrainCarriageQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainCarriageMapper.deleteByPrimaryKey(id);
    }

    public List<TrainCarriage> selectByTrainCode(String trainCode) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        TrainCarriageExample.Criteria criteria = trainCarriageExample.createCriteria();
        trainCarriageExample.setOrderByClause("`index` asc");
        criteria.andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }

}