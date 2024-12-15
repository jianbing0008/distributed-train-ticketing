package com.jiawa.train.member.controller;

import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.member.req.TicketQueryReq;
import com.jiawa.train.member.resp.TicketQueryResp;
import com.jiawa.train.member.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private static final Logger LOG = LoggerFactory.getLogger(TicketController.class);
    @Autowired
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> query(@Valid TicketQueryReq ticketQueryReq) {
        ticketQueryReq.setMemberId(LoginMemberContext.getId());
        PageResp<TicketQueryResp> ticketQueryResps = ticketService.queryList(ticketQueryReq);
        return new CommonResp<>(ticketQueryResps);
    }


}