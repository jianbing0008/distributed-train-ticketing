package com.jiawa.train.member.controller.admin;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.member.req.TicketQueryReq;
import com.jiawa.train.member.resp.TicketQueryResp;
import com.jiawa.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {
    @Autowired
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid  TicketQueryReq req) {
        PageResp<TicketQueryResp> list = ticketService.queryList(req);
        return new CommonResp<>(list);
    }
}