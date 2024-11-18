package com.jiawa.train.member.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class MemberSendCodeReq  {
    @Pattern(regexp = "^1\\d{10}$", message = "手机号码格式错误")
    @NotBlank(message = "【手机号】不能为空")
    private String mobile;
}
