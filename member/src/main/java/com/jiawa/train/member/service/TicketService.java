package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Ticket;
import com.jiawa.train.member.domain.TicketExample;
import com.jiawa.train.member.mapper.TicketMapper;
import com.jiawa.train.member.req.TicketQueryReq;
import com.jiawa.train.member.req.TicketSaveReq;
import com.jiawa.train.member.resp.TicketQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * Ticket服务类，负责处理与Ticket相关的业务逻辑
 */
public class TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    /**
     * 保存Ticket信息
     *
     * @param req Ticket保存请求对象，包含Ticket的基本信息
     */
    public void save(TicketSaveReq req){
        // 获取当前时间，用于记录Ticket信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为Ticket对象，便于后续操作
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增Ticket
            // 设置Ticket的会员ID，来源于登录会员上下文
            // 生成Ticket的唯一ID
            ticket.setId(SnowUtil.getSnowflakeNextId());
            // 设置Ticket信息的创建和更新时间为当前时间
            ticket.setCreateTime(now);
            ticket.setUpdateTime(now);
            // 插入Ticket信息到数据库
            ticketMapper.insert(ticket);
        }else{  // 不为空则更新Ticket信息
            ticket.setUpdateTime(now);
            ticketMapper.updateByPrimaryKey(ticket);
        }

    }

    /**
     * 查询Ticket列表
     *
     * @param req Ticket查询请求对象，可能包含Ticket的会员ID等查询条件
     */
    public PageResp<TicketQueryResp> queryList(TicketQueryReq req){
        // 创建Ticket示例对象，用于构造查询条件
        TicketExample ticketExample = new TicketExample();
        //根据id倒序排序
        ticketExample.setOrderByClause("id desc");
        // 创建查询条件对象
        TicketExample.Criteria criteria = ticketExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的Ticket信息
        List<Ticket> ticketList = ticketMapper.selectByExample(ticketExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<Ticket> pageInfo = new PageInfo<>(ticketList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<TicketQueryResp> list = BeanUtil.copyToList(ticketList, TicketQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<TicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        ticketMapper.deleteByPrimaryKey(id);
    }
}