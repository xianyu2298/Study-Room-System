package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    /**
     * 获取服务器时间（用于前端时间同步）
     */
    @GetMapping("/time")
    public R<Map<String, Long>> getServerTime() {
        Map<String, Long> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        data.put("date", System.currentTimeMillis());
        return R.ok(data);
    }
}
