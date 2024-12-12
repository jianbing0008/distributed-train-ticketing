package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Passenger;
import com.jiawa.train.member.domain.PassengerExample;
import com.jiawa.train.member.mapper.PassengerMapper;
import com.jiawa.train.member.req.PassengerQueryReq;
import com.jiawa.train.member.req.PassengerSaveReq;
import com.jiawa.train.member.resp.PassengerQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * Passenger服务类，负责处理与Passenger相关的业务逻辑
 */
public class PassengerService {

    @Autowired
    private PassengerMapper passengerMapper;

    /**
     * 保存Passenger信息
     *
     * @param req Passenger保存请求对象，包含Passenger的基本信息
     */
    public void save(PassengerSaveReq req){
        // 获取当前时间，用于记录Passenger信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为Passenger对象，便于后续操作
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增Passenger
            // 设置Passenger的会员ID，来源于登录会员上下文
            passenger.setMemberId(LoginMemberContext.getId());
            // 生成Passenger的唯一ID
            passenger.setId(SnowUtil.getSnowflakeNextId());
            // 设置Passenger信息的创建和更新时间为当前时间
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            // 插入Passenger信息到数据库
            passengerMapper.insert(passenger);
        }else{  // 不为空则更新Passenger信息
            passenger.setUpdateTime(now);
            passengerMapper.updateByPrimaryKey(passenger);
        }

    }

    /**
     * 查询Passenger列表
     *
     * @param req Passenger查询请求对象，可能包含Passenger的会员ID等查询条件
     */
    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req){
        // 创建Passenger示例对象，用于构造查询条件
        PassengerExample passengerExample = new PassengerExample();
        //根据id倒序排序
        passengerExample.setOrderByClause("id desc");
        // 创建查询条件对象
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        // 如果请求对象中的会员ID不为空，则添加会员ID作为查询条件
        if(ObjectUtil.isNotNull(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }

        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的Passenger信息
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<Passenger> pageInfo = new PageInfo<>(passengerList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<PassengerQueryResp> list = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        passengerMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查找我的所有乘客
     */
    public List<PassengerQueryResp> queryMine(){
        // 创建Passenger示例对象，用于构造查询条件
        PassengerExample passengerExample = new PassengerExample();
        //根据id倒序排序
        passengerExample.setOrderByClause("name asc");
        // 创建查询条件对象
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        // 如果请求对象中的会员ID不为空，则添加会员ID作为查询条件
        criteria.andMemberIdEqualTo(LoginMemberContext.getId());
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(passengerList, PassengerQueryResp.class);
    }
}