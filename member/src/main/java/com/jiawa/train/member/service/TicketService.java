package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.req.MemberTicketReq;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Ticket;
import com.jiawa.train.member.domain.TicketExample;
import com.jiawa.train.member.mapper.TicketMapper;
import com.jiawa.train.member.req.TicketQueryReq;
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



    public void save(MemberTicketReq req) {
        DateTime now = DateTime.now();
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        ticket.setId(SnowUtil.getSnowflakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        ticketMapper.insert(ticket);
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

        if(ObjectUtil.isNotNull(req.getMemberId())) {
            criteria.andMemberIdEqualTo(req.getMemberId());
        }

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