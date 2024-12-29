package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.business.domain.SkToken;
import com.jiawa.train.business.domain.SkTokenExample;
import com.jiawa.train.business.mapper.SkTokenMapper;
import com.jiawa.train.business.req.SkTokenQueryReq;
import com.jiawa.train.business.req.SkTokenSaveReq;
import com.jiawa.train.business.resp.SkTokenQueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * SkToken服务类，负责处理与SkToken相关的业务逻辑
 */
public class SkTokenService {

    @Autowired
    private SkTokenMapper skTokenMapper;

    /**
     * 保存SkToken信息
     *
     * @param req SkToken保存请求对象，包含SkToken的基本信息
     */
    public void save(SkTokenSaveReq req){
        // 获取当前时间，用于记录SkToken信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为SkToken对象，便于后续操作
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增SkToken
            // 设置SkToken的会员ID，来源于登录会员上下文
            // 生成SkToken的唯一ID
            skToken.setId(SnowUtil.getSnowflakeNextId());
            // 设置SkToken信息的创建和更新时间为当前时间
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            // 插入SkToken信息到数据库
            skTokenMapper.insert(skToken);
        }else{  // 不为空则更新SkToken信息
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }

    }

    /**
     * 查询SkToken列表
     *
     * @param req SkToken查询请求对象，可能包含SkToken的会员ID等查询条件
     */
    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req){
        // 创建SkToken示例对象，用于构造查询条件
        SkTokenExample skTokenExample = new SkTokenExample();
        //根据id倒序排序
        skTokenExample.setOrderByClause("id desc");
        // 创建查询条件对象
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的SkToken信息
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<SkToken> pageInfo = new PageInfo<>(skTokenList);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<SkTokenQueryResp> list = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        // 创建并组装分页响应对象
        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteByPrimaryKey(id);
    }
}