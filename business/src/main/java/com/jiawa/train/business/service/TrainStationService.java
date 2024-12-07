package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.TrainStation;
import com.jiawa.train.business.domain.TrainStationExample;
import com.jiawa.train.business.mapper.TrainStationMapper;
import com.jiawa.train.business.req.TrainStationQueryReq;
import com.jiawa.train.business.req.TrainStationSaveReq;
import com.jiawa.train.business.resp.TrainStationQueryResp;
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
 * TrainStation服务类，负责处理与TrainStation相关的业务逻辑
 */
public class TrainStationService {

    @Autowired
    private TrainStationMapper trainStationMapper;

    /**
     * 保存TrainStation信息
     *
     * @param req TrainStation保存请求对象，包含TrainStation的基本信息
     */
    public void save(TrainStationSaveReq req){
        // 获取当前时间，用于记录TrainStation信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为TrainStation对象，便于后续操作
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增TrainStation
            // 保存之前，先校验唯一键是否存在
            TrainStation trainStationDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if(ObjectUtil.isNotEmpty(trainStationDB) ){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }
            trainStationDB = selectByUnique(req.getTrainCode(), req.getName());
            if(ObjectUtil.isNotEmpty(trainStationDB)){
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }

            // 设置TrainStation的会员ID，来源于登录会员上下文
            // 生成TrainStation的唯一ID
            trainStation.setId(SnowUtil.getSnowflakeNextId());
            // 设置TrainStation信息的创建和更新时间为当前时间
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            // 插入TrainStation信息到数据库
            trainStationMapper.insert(trainStation);
        }else{  // 不为空则更新TrainStation信息
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }

    }
    private TrainStation selectByUnique(String trainCode, Integer index) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainStation> list = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }
    private TrainStation selectByUnique(String trainCode, String name) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andNameEqualTo(name);
        List<TrainStation> list = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    public List<TrainStation> selectByTrainingCode(String trainCode) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("`index` ASC");
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(trainStationExample);
    }

    /**
     * 查询TrainStation列表
     *
     * @param req TrainStation查询请求对象，可能包含TrainStation的会员ID等查询条件
     */
    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req){
        // 创建TrainStation示例对象，用于构造查询条件
        TrainStationExample trainStationExample = new TrainStationExample();
        //根据id倒序排序
        trainStationExample.setOrderByClause("train_code asc, `index` asc");
        // 创建查询条件对象
        TrainStationExample.Criteria criteria = trainStationExample.createCriteria();


        // 根据req对象中的trainCode属性值来决定是否添加查询条件
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            // 如果trainCode不为空，则添加查询条件，限定查询结果的trainCode必须与req对象中的trainCode相同
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的TrainStation信息
        List<TrainStation> trainStationList = trainStationMapper.selectByExample(trainStationExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<TrainStation> pageInfo = new PageInfo<>(trainStationList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteByPrimaryKey(id);
    }


}