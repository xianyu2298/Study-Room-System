package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 签到签退控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    /**
     * 签到
     * POST /api/sign/in/{reserveId}
     */
    @PostMapping("/in/{reserveId}")
    @RequiresPermissions("reserve:create")
    public R<Map<String, Object>> signIn(@PathVariable Long reserveId) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = signService.signIn(userId, reserveId);
        return R.ok("签到成功", result);
    }

    /**
     * 签退
     * POST /api/sign/out/{reserveId}
     */
    @PostMapping("/out/{reserveId}")
    @RequiresPermissions("reserve:create")
    public R<Map<String, Object>> signOut(@PathVariable Long reserveId) {
        Long userId = getCurrentUserId();
        Map<String, Object> result = signService.signOut(userId, reserveId);
        return R.ok("签退成功", result);
    }

    private Long getCurrentUserId() {
        return (Long) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
    }
}
