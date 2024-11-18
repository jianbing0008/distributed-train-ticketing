package com.jiawa.train.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.domain.MemberExample;
import com.jiawa.train.member.mapper.MemberMapper;
import com.jiawa.train.member.req.MemberRegisterReq;
import com.jiawa.train.member.req.MemberSendCodeReq;
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
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);

        if(CollUtil.isNotEmpty(list)){
            //return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member member = new Member();
        member.setId(SnowUtil.getSnowflakeNextId()); //雪花算法
        member.setMobile(mobile);
        memberMapper.insert(member);

        return member.getId();
    }

    public void sendCode(MemberSendCodeReq req) {
        // 校验手机号是否已注册
        String mobile = req.getMobile();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);

        // 如果手机号不存在，则插入记录
        if(CollUtil.isEmpty(list)){
            log.info("手机号不存在，插入记录，mobile={}", mobile);
            Member member = new Member();
            member.setId(SnowUtil.getSnowflakeNextId()); //雪花算法
            member.setMobile(mobile);
            memberMapper.insert(member);
        }else{
            log.info("手机号已存在，不插入记录，mobile={}", mobile);
        }

        //生成验证码
       // String code = RandomUtil.randomString(4);
        String code = "8888"; //方便测试
        log.info("生成短信验证码：{}", code);

        // 保存短信记录表: 手机号，验证码，有效期，是否已使用，业务类型，发送时间，使用时间
        log.info("保存短信记录表");

        // 对接短信通道，发送短信
        log.info("对接短信通道，发送短信");

    }
}
