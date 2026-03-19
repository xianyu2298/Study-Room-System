package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.dto.*;
import edu.jjxy.studyroom.backend.entity.vo.UserVo;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import edu.jjxy.studyroom.backend.util.EmailUtil;
import edu.jjxy.studyroom.backend.util.PasswordUtil;
import edu.jjxy.studyroom.backend.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CaptchaService captchaService;
    private final GraphicCaptchaService graphicCaptchaService;

    @Value("${system.login.max-error-count:5}")
    private int maxErrorCount;

    @Value("${system.login.lock-minutes:10}")
    private int lockMinutes;

    // ==================== 用户注册 ====================

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterDTO dto) {
        // 1. 校验图形验证码
        if (!graphicCaptchaService.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptcha())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }

        // 2. 校验密码确认
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "两次密码输入不一致");
        }

        // 3. 校验密码复杂度（后端二次校验）
        if (!PasswordUtil.checkPasswordComplexity(dto.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 4. 校验邮箱格式
        if (!EmailUtil.isValidEmail(dto.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_FORMAT_ERROR);
        }

        // 5. 检查学号唯一性
        User existStudentNo = findByStudentNo(dto.getStudentNo());
        if (existStudentNo != null) {
            throw new BusinessException(ResultCode.STUDENT_NO_ALREADY_EXISTS);
        }

        // 6. 检查邮箱唯一性
        User existEmail = findByEmail(dto.getEmail());
        if (existEmail != null) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 7. 发送邮箱验证码并校验
        captchaService.verifyEmailCode(dto.getEmail(), "register", dto.getEmailCode());

        // 8. 检查幂等性（防止重复提交）
        String idempotencyKey = Constants.REDIS_IDEMPOTENCY + "register:" + dto.getStudentNo();
        Boolean setIfAbsent = stringRedisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(setIfAbsent)) {
            throw new BusinessException(ResultCode.IDEMPOTENT_ERROR);
        }

        // 9. 创建用户
        User user = new User();
        user.setStudentNo(dto.getStudentNo());
        user.setEmail(dto.getEmail());
        user.setPassword(PasswordUtil.encryptPassword(dto.getPassword()));
        user.setName(dto.getName() == null || dto.getName().trim().isEmpty()
                ? Constants.DEFAULT_NAME_PREFIX_STUDENT + dto.getStudentNo().substring(dto.getStudentNo().length() - 6)
                : dto.getName());
        user.setRole(Constants.ROLE_STUDENT);
        user.setStatus(Constants.STATUS_NORMAL);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功 - studentNo: {}, email: {}", dto.getStudentNo(), dto.getEmail());

        return user;
    }

    // ==================== 用户登录 ====================

    /**
     * 用户登录
     */
    public LoginResult login(LoginDTO dto) {
        System.out.println(">>> 登录请求: username=" + dto.getUsername() + ", password=" + dto.getPassword() + ", captchaKey=" + dto.getCaptchaKey() + ", captcha=" + dto.getCaptcha());
        // 1. 校验图形验证码
        if (!graphicCaptchaService.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptcha())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }

        // 2. 获取用户（学号或邮箱）
        User user = findByStudentNoOrEmail(dto.getUsername());
        System.out.println(">>> 查到的用户: " + (user == null ? "null" : "id=" + user.getId() + ", email=" + user.getEmail() + ", status=" + user.getStatus() + ", password=" + user.getPassword()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 检查账号状态
        checkUserStatus(user);
        System.out.println(">>> 状态检查通过");

        // 4. 检查登录错误次数和锁定
        checkLoginErrorCount(user);

        // 5. 校验密码
        System.out.println(">>> 密码校验: 输入密码=" + dto.getPassword() + ", 数据库密码=" + user.getPassword() + ", 加密后=" + PasswordUtil.encryptPassword(dto.getPassword()));
        if (!PasswordUtil.verifyPassword(dto.getPassword(), user.getPassword())) {
            // 记录密码错误
            incrementLoginErrorCount(user);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        System.out.println(">>> 密码校验通过!");

        // 6. 登录成功，清除错误计数
        clearLoginErrorCount(user);

        // 7. 构建登录结果
        return buildLoginResult(user);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        // 检查是否被禁用
        if (user.getStatus() != null && user.getStatus().intValue() == Constants.STATUS_DISABLED) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 检查是否被锁定
        if (user.getStatus() != null && user.getStatus().intValue() == Constants.STATUS_LOCKED) {
            throw new BusinessException(ResultCode.USER_LOCKED);
        }

        // 检查是否被禁止预约
        if (user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.USER_BANNED, user.getBanEndTime().toString());
        }
    }

    /**
     * 检查登录错误次数
     */
    private void checkLoginErrorCount(User user) {
        String lockKey = Constants.REDIS_LOGIN_LOCK + user.getId();
        String lockValue = stringRedisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            long remainingSeconds = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            long remainingMinutes = (remainingSeconds + 59) / 60;
            throw new BusinessException(ResultCode.LOGIN_MAX_ERROR, maxErrorCount, remainingMinutes);
        }

        String countKey = Constants.REDIS_LOGIN_ERROR_COUNT + user.getId();
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        int errorCount = countStr == null ? 0 : Integer.parseInt(countStr);
        if (errorCount >= maxErrorCount) {
            // 锁定账号
            user.setStatus(Constants.STATUS_LOCKED);
            userMapper.updateById(user);

            // 设置Redis锁定标记
            stringRedisTemplate.opsForValue().set(lockKey, "1", Duration.ofMinutes(lockMinutes));
            stringRedisTemplate.delete(countKey);

            throw new BusinessException(ResultCode.LOGIN_MAX_ERROR, maxErrorCount, lockMinutes);
        }
    }

    /**
     * 增加登录错误计数
     */
    private void incrementLoginErrorCount(User user) {
        String countKey = Constants.REDIS_LOGIN_ERROR_COUNT + user.getId();
        Long count = stringRedisTemplate.opsForValue().increment(countKey);
        if (count != null && count == 1) {
            // 首次错误，设置过期时间
            stringRedisTemplate.expire(countKey, Duration.ofMinutes(lockMinutes));
        }
    }

    /**
     * 清除登录错误计数
     */
    private void clearLoginErrorCount(User user) {
        String countKey = Constants.REDIS_LOGIN_ERROR_COUNT + user.getId();
        String lockKey = Constants.REDIS_LOGIN_LOCK + user.getId();
        stringRedisTemplate.delete(countKey);
        stringRedisTemplate.delete(lockKey);

        // 如果账号状态是锁定状态，恢复为正常
        if (user.getStatus() != null && user.getStatus().intValue() == Constants.STATUS_LOCKED) {
            user.setStatus(Constants.STATUS_NORMAL);
            userMapper.updateById(user);
        }
    }

    /**
     * 构建登录结果
     */
    private LoginResult buildLoginResult(User user) {
        // 这里需要注入JWT服务，但为了避免循环依赖，在Controller层处理JWT生成
        LoginResult result = new LoginResult();
        result.setUserId(user.getId());
        result.setStudentNo(user.getStudentNo());
        result.setEmail(user.getEmail());
        result.setName(user.getName());
        result.setRole(user.getRole());
        result.setRoleName(getRoleName(user.getRole()));
        return result;
    }

    // ==================== 密码找回 ====================

    /**
     * 发送找回密码验证码
     */
    public void sendResetPwdCode(String email) {
        // 1. 检查邮箱是否存在
        User user = findByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 发送验证码
        captchaService.sendEmailCode(email, "resetPwd");
    }

    /**
     * 重置密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        // 1. 校验密码复杂度
        if (!PasswordUtil.checkPasswordComplexity(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 2. 查找用户
        User user = findByEmail(dto.getEmail());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 校验邮箱验证码
        captchaService.verifyEmailCode(dto.getEmail(), "resetPwd", dto.getCode());

        // 4. 更新密码
        user.setPassword(PasswordUtil.encryptPassword(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("密码重置成功 - email: {}", dto.getEmail());
    }

    // ==================== 个人信息 ====================

    /**
     * 获取用户信息
     */
    public UserVo getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserVo(user);
    }

    /**
     * 更新个人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVo updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新昵称
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        } else {
            // 未填写昵称时自动生成
            user.setName(generateDefaultName(user));
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return toUserVo(user);
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 校验原密码
        if (!PasswordUtil.verifyPassword(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 校验新密码复杂度
        if (!PasswordUtil.checkPasswordComplexity(dto.getNewPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_FORMAT_ERROR);
        }

        // 更新密码
        user.setPassword(PasswordUtil.encryptPassword(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("密码修改成功 - userId: {}", userId);
    }

    // ==================== 查询方法 ====================

    /**
     * 根据学号查询
     */
    public User findByStudentNo(String studentNo) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentNo, studentNo)
        );
    }

    /**
     * 根据邮箱查询
     */
    public User findByEmail(String email) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
    }

    /**
     * 根据学号或邮箱查询
     */
    public User findByStudentNoOrEmail(String username) {
        User user = findByStudentNo(username);
        if (user == null) {
            user = findByEmail(username);
        }
        return user;
    }

    /**
     * 根据ID查询
     */
    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    // ==================== 工具方法 ====================

    /**
     * 生成默认昵称
     */
    private String generateDefaultName(User user) {
        if (user.getRole() != null && user.getRole() == Constants.ROLE_STUDENT) {
            if (user.getStudentNo() != null && user.getStudentNo().length() >= 6) {
                return Constants.DEFAULT_NAME_PREFIX_STUDENT + user.getStudentNo().substring(user.getStudentNo().length() - 6);
            }
            return Constants.DEFAULT_NAME_PREFIX_STUDENT + EmailUtil.getEmailPrefix(user.getEmail());
        } else {
            return Constants.DEFAULT_NAME_PREFIX_ADMIN + (user.getId() != null ? user.getId().toString() : "");
        }
    }

    /**
     * 转换为VO
     */
    public UserVo toUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setStudentNo(user.getStudentNo());
        vo.setEmail(user.getEmail());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setRoleName(getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusName(getStatusName(user.getStatus()));
        vo.setBanEndTime(user.getBanEndTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(Integer role) {
        if (role == null) return "";
        switch (role) {
            case Constants.ROLE_STUDENT:
                return "学生";
            case Constants.ROLE_ADMIN:
                return "管理员";
            case Constants.ROLE_SUPER_ADMIN:
                return "超级管理员";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case Constants.STATUS_NORMAL:
                return "正常";
            case Constants.STATUS_DISABLED:
                return "禁用";
            case Constants.STATUS_LOCKED:
                return "锁定";
            default:
                return "未知";
        }
    }

    /**
     * 登录结果内部类
     */
    @lombok.Data
    public static class LoginResult {
        private Long userId;
        private String studentNo;
        private String email;
        private String name;
        private Integer role;
        private String roleName;
        private String token;
    }

}
