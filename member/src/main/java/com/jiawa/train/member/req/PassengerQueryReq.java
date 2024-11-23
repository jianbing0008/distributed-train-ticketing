package com.jiawa.train.member.req;

import com.jiawa.train.common.req.PageReq;

public class PassengerQueryReq extends PageReq {

    private Long memberId;


    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", memberId=").append(memberId);
        sb.append("]");
        return sb.toString();
    }
}