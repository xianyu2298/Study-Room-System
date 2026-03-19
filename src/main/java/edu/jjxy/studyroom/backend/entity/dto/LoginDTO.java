package edu.jjxy.studyroom.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户登录DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    /**
     * 学号/邮箱
     */
    @NotBlank(message = "学号/邮箱不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 图形验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 图形验证码Key
     */
    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;
}
