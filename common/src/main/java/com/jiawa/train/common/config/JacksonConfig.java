package com.jiawa.train.common.config;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.module.SimpleModule;
//import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
//
///**
//  * 统一注解，解决前后端交互Long类型精度丢失的问题
//  * 但是也会把一些很小数值的long类型也转成字符串，例如分页的total，这样前端会多一些警告
//  * https://coding.imooc.com/class/474.html
//  */
// /**
//  * Jackson配置类，用于配置ObjectMapper
//  */
// @Configuration
// public class JacksonConfig {
//
//     /**
//      * 创建并配置ObjectMapper实例
//      *
//      * @param builder Jackson2ObjectMapperBuilder的实例，用于构建ObjectMapper实例
//      * @return 返回配置好的ObjectMapper实例
//      */
//     @Bean
//     public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
//         // 创建一个不处理XML的ObjectMapper实例
//         ObjectMapper objectMapper = builder.createXmlMapper(false).build();
//
//         // 创建一个SimpleModule实例，用于注册自定义的序列化器或反序列化器
//         SimpleModule simpleModule = new SimpleModule();
//
//         // 为Long类型添加一个序列化器，将Long值序列化为字符串
//         // 这是为了避免在某些情况下，Long类型的数据在序列化成JSON时可能会丢失精度
//         simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//
//         // 将自定义的模块注册到ObjectMapper中
//         objectMapper.registerModule(simpleModule);
//
//         // 返回配置好的ObjectMapper实例
//         return objectMapper;
//     }
// }