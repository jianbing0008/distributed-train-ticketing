package com.jiawa.train.member.req;

import com.jiawa.train.common.req.PageReq;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TicketQueryReq extends PageReq {

   private Long memberId;
}