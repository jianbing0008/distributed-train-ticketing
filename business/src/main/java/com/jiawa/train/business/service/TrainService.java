package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.Train;
import com.jiawa.train.business.domain.TrainExample;
import com.jiawa.train.business.mapper.TrainMapper;
import com.jiawa.train.business.req.TrainQueryReq;
import com.jiawa.train.business.req.TrainSaveReq;
import com.jiawa.train.business.resp.TrainQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * Train服务类，负责处理与Train相关的业务逻辑
 */
public class TrainService {

    @Autowired
    private TrainMapper trainMapper;

    /**
     * 保存Train信息
     *
     * @param req Train保存请求对象，包含Train的基本信息
     */
    public void save(TrainSaveReq req){
        // 获取当前时间，用于记录Train信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为Train对象，便于后续操作
        Train train = BeanUtil.copyProperties(req, Train.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增Train
            // 设置Train的会员ID，来源于登录会员上下文
            // 生成Train的唯一ID
            train.setId(SnowUtil.getSnowflakeNextId());
            // 设置Train信息的创建和更新时间为当前时间
            train.setCreateTime(now);
            train.setUpdateTime(now);
            // 插入Train信息到数据库
            trainMapper.insert(train);
        }else{  // 不为空则更新Train信息
            train.setUpdateTime(now);
            trainMapper.updateByPrimaryKey(train);
        }

    }

    /**
     * 查询Train列表
     *
     * @param req Train查询请求对象，可能包含Train的会员ID等查询条件
     */
    public PageResp<TrainQueryResp> queryList(TrainQueryReq req){
        // 创建Train示例对象，用于构造查询条件
        TrainExample trainExample = new TrainExample();
        //根据id倒序排序
        trainExample.setOrderByClause("id desc");
        // 创建查询条件对象
        TrainExample.Criteria criteria = trainExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的Train信息
        List<Train> trainList = trainMapper.selectByExample(trainExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<Train> pageInfo = new PageInfo<>(trainList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<TrainQueryResp> list = BeanUtil.copyToList(trainList, TrainQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<TrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainMapper.deleteByPrimaryKey(id);
    }

    public List<TrainQueryResp> queryAll(){
        // 创建Train示例对象，用于构造查询条件
        TrainExample trainExample = new TrainExample();
        //根据code(车次编号)倒序排序
        trainExample.setOrderByClause("id desc");
        // 根据构造的查询条件，从数据库中选择符合条件的Train信息
        List<Train> trainList = trainMapper.selectByExample(trainExample);
        // 将查询结果列表转换为目标响应对象列表
        return BeanUtil.copyToList(trainList, TrainQueryResp.class);
    }
}