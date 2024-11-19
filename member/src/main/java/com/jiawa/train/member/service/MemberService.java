package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import com.jiawa.train.member.req.MemberLoginReq;
import com.jiawa.train.member.req.MemberRegisterReq;
import com.jiawa.train.member.req.MemberSendCodeReq;
import com.jiawa.train.member.resp.MemberLoginResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberMapper memberMapper;
    public int count() {
       return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req) {
        String mobile = req.getMobile();
        Member memberDB = getByMobile(mobile);

        if(ObjectUtil.isNotNull(memberDB)){
            //return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member member = new Member();
        member.setId(SnowUtil.getSnowflakeNextId()); //雪花算法
        member.setMobile(mobile);
        memberMapper.insert(member);

        return member.getId();
    }

    /**
     * 发送验证码方法
     * 当手机号未注册时，自动创建用户，并发送验证码
     *
     * @param req 包含手机号的请求对象
     */
    public void sendCode(MemberSendCodeReq req) {
        // 校验手机号是否已注册
        String mobile = req.getMobile();
        Member memberDB = getByMobile(mobile);

        // 如果手机号不存在，则插入记录
        if(ObjectUtil.isNull(memberDB)){
            log.info("手机号不存在，插入记录，mobile={}", mobile);
            Member member = new Member();
            member.setId(SnowUtil.getSnowflakeNextId()); // 使用雪花算法生成唯一ID
            member.setMobile(mobile);
            memberMapper.insert(member);
        }else{
            log.info("手机号已存在，不插入记录，mobile={}", mobile);
        }

        // 生成验证码，为了方便测试，这里直接使用"8888"，在实际应用中应使用随机生成的验证码
        // String code = RandomUtil.randomString(4);
        String code = "8888";
        log.info("生成短信验证码：{}", code);

        // 保存短信记录表: 包括手机号，验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        log.info("保存短信记录表");

        // 对接短信通道，发送短信
        log.info("对接短信通道，发送短信");
    }

    /**
     * 登录方法
     * 通过手机号和验证码进行用户登录验证
     *
     * @param req 包含手机号和验证码的请求对象
     * @return 登录成功的用户信息响应对象
     * @throws BusinessException 当手机号未注册或验证码错误时抛出业务异常
     */
    public MemberLoginResp login(MemberLoginReq req) {
        // 校验手机号是否已注册
        String mobile = req.getMobile();
        String code = req.getCode();
        Member memberDB = getByMobile(mobile);

        // 如果手机号不存在，则抛出异常提示
        if(ObjectUtil.isNull(memberDB)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        // 校验验证码，如果不匹配，则抛出异常提示
        if(!"8888".equals(code)){
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }

        // 验证通过，返回用户信息
        return BeanUtil.copyProperties(memberDB, MemberLoginResp.class);
    }

    /**
     * 根据手机号码获取会员信息
     *
     * 此方法通过手机号码查询会员数据库，如果找到匹配的会员，则返回该会员信息；
     * 如果没有找到匹配的会员，则返回null此方法体现了通过特定条件查询数据库
     * 并处理查询结果的典型逻辑
     *
     * @param mobile 手机号码，用于查询会员信息的唯一标识
     * @return 如果找到匹配的会员，则返回会员对象；否则返回null
     */
    private Member getByMobile(String mobile) {
        // 创建一个MemberExample对象，用于设置查询条件
        MemberExample memberExample = new MemberExample();
        // 设置查询条件：根据手机号码查询
        memberExample.createCriteria().andMobileEqualTo(mobile);
        // 执行查询，获取符合条件的会员列表
        List<Member> list = memberMapper.selectByExample(memberExample);

        // 判断查询结果是否为空
        if(CollUtil.isEmpty(list)){
            // 如果查询结果为空，返回null
            return null;
        }else{
            // 如果查询结果不为空，返回第一个会员对象
            return list.get(0);
        }
    }
}
