package com.jiawa.train.common.controller;

import cn.hutool.core.util.StrUtil;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.resp.CommonResp;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理、数据预处理等
 */
@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    // 日志对象，用于记录异常信息
    private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    /**
     * 所有异常统一处理
     * @param e 异常对象
     * @return 返回包含错误信息的CommonResp对象
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public CommonResp exceptionHandler(Exception e) throws Exception {
        log.info("seata全局事务ID save：{}", RootContext.getXID());
        //如果是在一次全局事务里抛出异常了,就不要包装返回值,将异常抛给调用方,让调用方回滚事务
        if(StrUtil.isNotBlank(RootContext.getXID())){
            throw e;
        }
        // 初始化一个成功的响应对象
        CommonResp commonResp = new CommonResp();
        // 记录系统异常信息
        LOG.error("系统异常：", e);
        // 设置响应对象为失败，并返回系统默认错误信息
        commonResp.setSuccess(false);
        commonResp.setMessage("系统出现异常，请联系管理员");
        return commonResp;
    }

    /**
     * 业务异常统一处理
     * @param e 业务异常对象
     * @return 返回包含业务错误信息的CommonResp对象
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public CommonResp exceptionHandler(BusinessException e) {
        // 初始化一个成功的响应对象
        CommonResp commonResp = new CommonResp();
        // 记录业务异常信息
        LOG.error("业务异常：{}", e.getE().getDesc());
        // 设置响应对象为失败，并返回具体的业务错误信息
        commonResp.setSuccess(false);
        // 将异常信息设置到响应对象中
        commonResp.setMessage(e.getE().getDesc());
        return commonResp;
    }

    /**
     * 校验异常统一处理
     * @param e 校验异常对象
     * @return 返回包含校验错误信息的CommonResp对象
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public CommonResp exceptionHandler(BindException e) {
        // 初始化一个成功的响应对象
        CommonResp commonResp = new CommonResp();
        // 记录校验异常信息
        LOG.error("校验异常：{}", e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        // 设置响应对象为失败，并返回具体的校验错误信息
        commonResp.setSuccess(false);
        commonResp.setMessage(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return commonResp;
    }

}
