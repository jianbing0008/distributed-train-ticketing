package com.jiawa.train.gateway.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // 日志记录器，用于记录JWTUtil类的操作日志
    private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 盐值很重要，不能泄漏，且每个项目都应该不一样，可以放到配置文件中
     */
    private static final String key = "JianBing12306";

    /**
     * 创建JWT token
     *
     * @param id 用户ID，用于标识用户
     * @param mobile 用户手机号，用于进一步标识用户
     * @return 返回生成的JWT token字符串
     */
    public static String createToken(Long id, String mobile) {
        // 当前时间
        DateTime now = DateTime.now();
        // token过期时间，这里设置为24小时后
        DateTime expTime = now.offsetNew(DateField.HOUR, 24);

        // 创建载荷，用于存放token的各种信息
        Map<String, Object> payload = new HashMap<>();
        // 签发时间
        payload.put(JWTPayload.ISSUED_AT, now);
        // 过期时间
        payload.put(JWTPayload.EXPIRES_AT, expTime);
        // 生效时间
        payload.put(JWTPayload.NOT_BEFORE, now);
        // 内容，包括用户ID和手机号
        payload.put("id", id);
        payload.put("mobile", mobile);
        // 使用私钥生成token
        String token = JWTUtil.createToken(payload, key.getBytes());

        // 记录生成的token
        LOG.info("生成JWT token：{}", token);
        return token;
    }

    /**
     * 校验JWT token的有效性
     *
     * @param token 待校验的JWT token字符串
     * @return 返回校验结果，true表示有效，false表示无效
     */
    public static boolean validate(String token) {
        try {
            // 解析token并设置密钥
            JWT jwt = JWTUtil.parseToken(token).setKey(key.getBytes());
            // 校验token的有效性，validate包含了verify
            boolean validate = jwt.validate(0);
            // 记录校验结果
            LOG.info("JWT token校验结果：{}", validate);
            return validate;
        } catch (Exception e) {
            LOG.error("JWT token校验异常",e);
            return false;
        }
    }

    /**
     * 从JWT token中获取载荷信息
     *
     * @param token JWT token字符串
     * @return 返回载荷信息的JSONObject对象，不包含签发时间、过期时间和生效时间
     */
    public static JSONObject getJSONObject(String token) {
        // 解析token并设置密钥
        JWT jwt = JWTUtil.parseToken(token).setKey(key.getBytes());
        // 获取载荷信息
        JSONObject payloads = jwt.getPayloads();
        // 移除签发时间、过期时间和生效时间，只保留特定的用户信息
        payloads.remove(JWTPayload.ISSUED_AT);
        payloads.remove(JWTPayload.EXPIRES_AT);
        payloads.remove(JWTPayload.NOT_BEFORE);
        // 记录载荷信息
        LOG.info("根据token获取原始内容：{}", payloads);
        return payloads;
    }


    public static void main(String[] args) {
        createToken(1L, "123");

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYmYiOjE3MDIxNzQxNzcsIm1vYmlsZSI6IjEyMyIsImlkIjoxLCJleHAiOjE3MDIyNjA1NzcsImlhdCI6MTcwMjE3NDE3N30.MmyTNBN9EkmKdeaqCheB2xpF9ifB5Lm0kKl5BIm8Pro";
        validate(token);

        getJSONObject(token);
    }
}