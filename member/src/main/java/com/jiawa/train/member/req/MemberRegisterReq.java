package com.jiawa.train.member.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class MemberRegisterReq {
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;
}
