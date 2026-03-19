package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.service.GraphicCaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图形验证码控制器
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final GraphicCaptchaService graphicCaptchaService;

    /**
     * 获取图形验证码
     */
    @GetMapping("/get")
    public Map<String, String> getCaptcha() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String imageBase64 = graphicCaptchaService.generateCaptcha(key);

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", "data:image/png;base64," + imageBase64);
        return result;
    }
}
