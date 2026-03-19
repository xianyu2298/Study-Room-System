package edu.jjxy.studyroom.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {

    /**
     * 学号
     */
    @NotBlank(message = "学号不能为空")
    @Size(min = 6, max = 20, message = "学号长度必须在6-20位之间")
    private String studentNo;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$", message = "密码需至少6位，且包含数字和字母")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 昵称（非必填）
     */
    private String name;

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

    /**
     * 邮箱验证码（注册时发送到邮箱）
     */
    private String emailCode;
}
