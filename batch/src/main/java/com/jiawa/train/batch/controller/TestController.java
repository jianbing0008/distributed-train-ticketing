package com.jiawa.train.batch.controller;

import com.jiawa.train.batch.feign.BusinessFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {
    @Autowired
    private BusinessFeign businessFeign;

    @GetMapping("/hello")
    public String hello() {
        String businessHello = businessFeign.hello();
        log.info("businessHello: {}", businessHello);
        return "hello world! Batch!"+ businessHello;
    }
}
