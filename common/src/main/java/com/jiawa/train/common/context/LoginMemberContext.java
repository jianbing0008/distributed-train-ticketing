package com.jiawa.train.common.context;


import com.jiawa.train.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginMemberContext {

    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);
    //定义通过线程本地变量存储会员登录基本信息    减少获取每一次获取用户基本信息的接口编写
    private static ThreadLocal<MemberLoginResp> member = new ThreadLocal<>();

    public static MemberLoginResp getMember() {
        return LoginMemberContext.member.get();
    }

    public static void setMember(MemberLoginResp member) {
        LOG.info("本地线程用户基本信息存储：{}",member);
        LoginMemberContext.member.set(member);
    }

    public static Long getId() {
        try {
            LOG.info("本地线程获取用户基本信息的Id:{}",LoginMemberContext.getMember().getId());
            return LoginMemberContext.getMember().getId();
        } catch (Exception e) {
            LOG.error("获取会员信息状态异常",e);
            throw e;
        }
    }
}