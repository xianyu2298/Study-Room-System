package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.dto.*;
import edu.jjxy.studyroom.backend.entity.vo.UserVo;
import edu.jjxy.studyroom.backend.service.CaptchaService;
import edu.jjxy.studyroom.backend.service.GraphicCaptchaService;
import edu.jjxy.studyroom.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CaptchaService captchaService;
    private final GraphicCaptchaService graphicCaptchaService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<Map<String, Object>> register(@Validated @RequestBody RegisterDTO dto) {
        // 注册
        userService.register(dto);

        // 生成Token
        // 注意：注册后需要重新登录获取Token
        Map<String, Object> data = new HashMap<>();
        data.put("message", "注册成功，请登录");

        return R.ok("注册成功", data);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Validated @RequestBody LoginDTO dto) {
        // 登录验证
        UserService.LoginResult loginResult = userService.login(dto);

        // 生成JWT Token
        String token = jwtUtil.generateToken(loginResult.getUserId(), loginResult.getRole());
        loginResult.setToken(token);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", loginResult.getUserId());
        data.put("studentNo", loginResult.getStudentNo());
        data.put("email", loginResult.getEmail());
        data.put("name", loginResult.getName());
        data.put("role", loginResult.getRole());
        data.put("roleName", loginResult.getRoleName());

        log.info("用户登录成功 - userId: {}, studentNo: {}", loginResult.getUserId(), loginResult.getStudentNo());

        return R.ok("登录成功", data);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        // 在实际项目中，可以将Token加入黑名单
        // 这里暂时不做处理，由前端清除Token
        return R.<Void>ok("登出成功", null);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    public R<Void> sendEmailCode(@Validated @RequestBody SendEmailCodeDTO dto) {
        captchaService.sendEmailCode(dto.getEmail(), dto.getType());
        return R.<Void>ok("验证码发送成功", null);
    }

    /**
     * 找回密码 - 发送验证码
     */
    @PostMapping("/sendResetCode")
    public R<Void> sendResetCode(@RequestParam String email) {
        userService.sendResetPwdCode(email);
        return R.<Void>ok("验证码发送成功", null);
    }

    /**
     * 找回密码 - 重置密码
     */
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return R.<Void>ok("密码重置成功，请使用新密码登录", null);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<UserVo> getUserInfo(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        UserVo userVo = userService.getUserInfo(userId);
        return R.ok(userVo);
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/info")
    public R<UserVo> updateProfile(@Validated @RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        UserVo userVo = userService.updateProfile(userId, dto);
        return R.ok("个人信息更新成功", userVo);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R<Void> changePassword(@Validated @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        userService.changePassword(userId, dto);
        return R.<Void>ok("密码修改成功", null);
    }

    /**
     * 获取服务器时间（用于前端时间同步）
     */
    @GetMapping("/serverTime")
    public R<Map<String, Long>> getServerTime() {
        Map<String, Long> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        return R.ok(data);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeaderName());
        if (token != null && token.startsWith(jwtUtil.getTokenPrefix())) {
            token = token.substring(jwtUtil.getTokenPrefix().length() + 1);
            return jwtUtil.getUserId(token);
        }
        throw new edu.jjxy.studyroom.backend.common.BusinessException(ResultCode.TOKEN_INVALID);
    }
}
