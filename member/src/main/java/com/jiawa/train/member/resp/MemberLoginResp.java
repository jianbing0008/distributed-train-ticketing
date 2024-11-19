package com.jiawa.train.member.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginResp {
    private Long id;

    private String mobile;

    private String token;


}