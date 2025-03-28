package com.jiawa.train.business.controller;

import com.jiawa.train.business.resp.StationQueryResp;
import com.jiawa.train.business.service.StationService;
import com.jiawa.train.common.resp.CommonResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("station")
public class StationController {
    @Autowired
    private StationService stationService;

    @GetMapping("/query-all")
    public CommonResp<List<StationQueryResp>> queryList() {
        List<StationQueryResp> list = stationService.queryAll();
        return new CommonResp<>(list);
    }

}