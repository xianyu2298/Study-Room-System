package edu.jjxy.studyroom.backend.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务
 */
@Slf4j
@Service
public class GraphicCaptchaService {

    private final StringRedisTemplate stringRedisTemplate;

    public GraphicCaptchaService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成图形验证码
     * @param key 验证码key
     * @return 验证码图片Base64编码
     */
    public String generateCaptcha(String key) {
        // 生成4位数字验证码
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        captcha.setGenerator(randomGenerator);
        captcha.setBackground(Color.white);

        // 生成验证码
        String code = captcha.getCode();
        String imageBase64 = captcha.getImageBase64();

        // 存储验证码（5分钟有效）
        stringRedisTemplate.opsForValue().set("captcha:" + key, code, 5, TimeUnit.MINUTES);

        log.debug("图形验证码生成 - key: {}, code: {}", key, code);

        return imageBase64;
    }

    /**
     * 校验图形验证码
     * @param key 验证码key
     * @param code 用户输入的验证码
     * @return 是否正确
     */
    public boolean verifyCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }

        String storedCode = stringRedisTemplate.opsForValue().get("captcha:" + key);
        if (storedCode == null) {
            return false;
        }

        // 忽略大小写比较
        boolean result = storedCode.equalsIgnoreCase(code);

        // 验证成功后删除验证码（一次性使用）
        if (result) {
            stringRedisTemplate.delete("captcha:" + key);
        }

        return result;
    }

    /**
     * 删除验证码
     */
    public void deleteCaptcha(String key) {
        stringRedisTemplate.delete("captcha:" + key);
    }
}
