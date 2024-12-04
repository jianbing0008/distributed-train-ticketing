package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.Station;
import com.jiawa.train.business.domain.StationExample;
import com.jiawa.train.business.mapper.StationMapper;
import com.jiawa.train.business.req.StationQueryReq;
import com.jiawa.train.business.req.StationSaveReq;
import com.jiawa.train.business.resp.StationQueryResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * Station服务类，负责处理与Station相关的业务逻辑
 */
public class StationService {

    @Autowired
    private StationMapper stationMapper;

    /**
     * 保存Station信息
     *
     * @param req Station保存请求对象，包含Station的基本信息
     */
    public void save(StationSaveReq req){
        // 获取当前时间，用于记录Station信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为Station对象，便于后续操作
        Station station = BeanUtil.copyProperties(req, Station.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增Station
            // 设置Station的会员ID，来源于登录会员上下文
            // 生成Station的唯一ID
            station.setId(SnowUtil.getSnowflakeNextId());
            // 设置Station信息的创建和更新时间为当前时间
            station.setCreateTime(now);
            station.setUpdateTime(now);
            // 插入Station信息到数据库
            stationMapper.insert(station);
        }else{  // 不为空则更新Station信息
            station.setUpdateTime(now);
            stationMapper.updateByPrimaryKey(station);
        }

    }

    /**
     * 查询Station列表
     *
     * @param req Station查询请求对象，可能包含Station的会员ID等查询条件
     */
    public PageResp<StationQueryResp> queryList(StationQueryReq req){
        // 创建Station示例对象，用于构造查询条件
        StationExample stationExample = new StationExample();
        //根据id倒序排序
        stationExample.setOrderByClause("id desc");
        // 创建查询条件对象
        StationExample.Criteria criteria = stationExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的Station信息
        List<Station> stationList = stationMapper.selectByExample(stationExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<Station> pageInfo = new PageInfo<>(stationList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<StationQueryResp> list = BeanUtil.copyToList(stationList, StationQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        stationMapper.deleteByPrimaryKey(id);
    }

    public List<StationQueryResp> queryAll(){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("name_pinyin asc");
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        return BeanUtil.copyToList(stationList, StationQueryResp.class);
    }
}