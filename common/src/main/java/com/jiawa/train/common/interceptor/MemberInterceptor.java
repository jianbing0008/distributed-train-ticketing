package com.jiawa.train.common.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.MemberLoginResp;
import com.jiawa.train.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截器：Spring框架特有的，常用于登录校验，权限校验，请求日志打印  其他模块按需添加
 */
@Component
public class MemberInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MemberInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.info("MemberInterceptor开始");
        String token = request.getHeader("token");
        if(StrUtil.isNotBlank(token)) {
            LOG.info("获取会员登录token：{}", token);
            JSONObject jsonObject = JwtUtil.getJSONObject(token);
            MemberLoginResp member = BeanUtil.copyProperties(jsonObject,MemberLoginResp.class);
            LOG.info("当前登录会员：{}", member);
            LoginMemberContext.setMember(member);
        }
        LOG.info("MemberInterceptor结束");
        return true;
    }
}
