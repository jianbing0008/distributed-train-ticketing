package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.business.req.DailyTrainTicketQueryReq;
import com.jiawa.train.business.req.DailyTrainTicketSaveReq;
import com.jiawa.train.business.resp.DailyTrainTicketQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * DailyTrainTicket服务类，负责处理与DailyTrainTicket相关的业务逻辑
 */
public class DailyTrainTicketService {

    @Autowired
    private DailyTrainTicketMapper dailyTrainTicketMapper;

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
        dailyTrainTicketExample.setOrderByClause("id desc");
        // 创建查询条件对象
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();


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
}