package edu.jjxy.studyroom.backend.service;

import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import edu.jjxy.studyroom.backend.util.EmailUtil;
import edu.jjxy.studyroom.backend.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * 生成4位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000);
        return String.valueOf(code);
    }

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param type 类型：register=注册，resetPwd=找回密码
     */
    public void sendEmailCode(String email, String type) {
        // 1. 检查发送间隔（1分钟内不能重复发送）
        String intervalKey = Constants.REDIS_EMAIL_SEND_INTERVAL + email;
        Boolean hasInterval = stringRedisTemplate.hasKey(intervalKey);
        if (Boolean.TRUE.equals(hasInterval)) {
            throw new edu.jjxy.studyroom.backend.common.BusinessException(ResultCode.EMAIL_CODE_FREQUENT);
        }

        // 2. 检查每日发送次数（最多5次）
        String dailyKey = Constants.REDIS_EMAIL_SEND_DAILY + email;
        String dailyCountStr = stringRedisTemplate.opsForValue().get(dailyKey);
        int dailyCount = dailyCountStr == null ? 0 : Integer.parseInt(dailyCountStr);
        if (dailyCount >= 5) {
            throw new edu.jjxy.studyroom.backend.common.BusinessException(ResultCode.EMAIL_CODE_DAILY_LIMIT);
        }

        // 3. 生成验证码
        String code = generateCode();

        // 4. 存储验证码（5分钟有效）
        String codeKey = Constants.REDIS_EMAIL_CODE + type + ":" + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(5));

        // 5. 设置发送间隔（60秒）
        stringRedisTemplate.opsForValue().set(intervalKey, "1", Duration.ofSeconds(60));

        // 6. 增加每日发送次数
        stringRedisTemplate.opsForValue().increment(dailyKey);
        // 设置每日计数过期时间（次日0点过期）
        stringRedisTemplate.expire(dailyKey, getSecondsToNextDay(), TimeUnit.SECONDS);

        // 7. 发送邮件
        String subject = "自习室预约系统 - 邮箱验证码";
        String content = buildEmailContent(code, type);
        sendEmail(email, subject, content);
    }

    /**
     * 校验邮箱验证码
     * @param email 邮箱
     * @param type 类型
     * @param code 用户输入的验证码
     */
    public void verifyEmailCode(String email, String type, String code) {
        String codeKey = Constants.REDIS_EMAIL_CODE + type + ":" + email;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new edu.jjxy.studyroom.backend.common.BusinessException(ResultCode.EMAIL_CODE_EXPIRED);
        }

        if (!storedCode.equals(code)) {
            throw new edu.jjxy.studyroom.backend.common.BusinessException(ResultCode.EMAIL_CODE_ERROR);
        }

        // 验证成功后删除验证码（一次性使用）
        stringRedisTemplate.delete(codeKey);
    }

    /**
     * 获取验证码（用于测试或重发）
     */
    public String getEmailCode(String email, String type) {
        String codeKey = Constants.REDIS_EMAIL_CODE + type + ":" + email;
        return stringRedisTemplate.opsForValue().get(codeKey);
    }

    /**
     * 清除邮箱验证码
     */
    public void clearEmailCode(String email, String type) {
        String codeKey = Constants.REDIS_EMAIL_CODE + type + ":" + email;
        stringRedisTemplate.delete(codeKey);
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功 - to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败 - to: {}, error: {}", to, e.getMessage());
            throw new edu.jjxy.studyroom.backend.common.BusinessException(
                    edu.jjxy.studyroom.backend.common.ResultCode.INTERNAL_SERVER_ERROR,
                    "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String type) {
        String action = "注册";
        if ("resetPwd".equals(type)) {
            action = "找回密码";
        }
        return String.format(
                "【自习室预约系统】\n\n" +
                "您正在进行的操作：%s\n\n" +
                "验证码：%s\n\n" +
                "验证码5分钟内有效，请勿泄露给他人。\n\n" +
                "如果不是您本人操作，请忽略此邮件。",
                action, code
        );
    }

    /**
     * 计算距离次日0点的秒数
     */
    private long getSecondsToNextDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return Duration.between(now, nextDay).getSeconds();
    }
}
