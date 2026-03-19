package edu.jjxy.studyroom.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 发送邮箱验证码DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailCodeDTO {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 类型：register=注册，resetPwd=找回密码
     */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(register|resetPwd)$", message = "类型参数不正确")
    private String type;
}
