package com.jiawa.train.${module}.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.${module}.domain.${Domain};
import com.jiawa.train.${module}.domain.${Domain}Example;
import com.jiawa.train.${module}.mapper.${Domain}Mapper;
import com.jiawa.train.${module}.req.${Domain}QueryReq;
import com.jiawa.train.${module}.req.${Domain}SaveReq;
import com.jiawa.train.${module}.resp.${Domain}QueryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * ${Domain}服务类，负责处理与${Domain}相关的业务逻辑
 */
public class ${Domain}Service {

    @Autowired
    private ${Domain}Mapper ${domain}Mapper;

    /**
     * 保存${Domain}信息
     *
     * @param req ${Domain}保存请求对象，包含${Domain}的基本信息
     */
    public void save(${Domain}SaveReq req){
        // 获取当前时间，用于记录${Domain}信息的创建和更新时间
        DateTime now = DateTime.now();
        // 将请求对象转换为${Domain}对象，便于后续操作
        ${Domain} ${domain} = BeanUtil.copyProperties(req, ${Domain}.class);
        if(ObjectUtil.isNull(req.getId())){ // 判断是否为空，为空则是新增${Domain}
            // 设置${Domain}的会员ID，来源于登录会员上下文
            // 生成${Domain}的唯一ID
            ${domain}.setId(SnowUtil.getSnowflakeNextId());
            // 设置${Domain}信息的创建和更新时间为当前时间
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            // 插入${Domain}信息到数据库
            ${domain}Mapper.insert(${domain});
        }else{  // 不为空则更新${Domain}信息
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.updateByPrimaryKey(${domain});
        }

    }

    /**
     * 查询${Domain}列表
     *
     * @param req ${Domain}查询请求对象，可能包含${Domain}的会员ID等查询条件
     */
    public PageResp<${Domain}QueryResp> queryList(${Domain}QueryReq req){
        // 创建${Domain}示例对象，用于构造查询条件
        ${Domain}Example ${domain}Example = new ${Domain}Example();
        //根据id倒序排序
        ${domain}Example.setOrderByClause("id desc");
        // 创建查询条件对象
        ${Domain}Example.Criteria criteria = ${domain}Example.createCriteria();


        // 记录查询日志
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());
        // 启用分页查询
        PageHelper.startPage(req.getPage(),req.getSize());
        // 根据构造的查询条件，从数据库中选择符合条件的${Domain}信息
        List<${Domain}> ${domain}List = ${domain}Mapper.selectByExample(${domain}Example);

        // 创建PageInfo对象，用于获取分页信息
        PageInfo<${Domain}> pageInfo = new PageInfo<>(${domain}List);
        // 记录分页信息日志
        log.info("总行数：{}", pageInfo.getTotal());
        log.info("总页数：{}", pageInfo.getPages());

        // 将查询结果列表转换为目标响应对象列表
        List<${Domain}QueryResp> list = BeanUtil.copyToList(${domain}List, ${Domain}QueryResp.class);

        // 创建并组装分页响应对象
        PageResp<${Domain}QueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        ${domain}Mapper.deleteByPrimaryKey(id);
    }
}