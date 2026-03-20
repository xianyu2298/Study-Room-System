package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.dto.ConfigDTO;
import edu.jjxy.studyroom.backend.entity.Config;
import edu.jjxy.studyroom.backend.mapper.ConfigMapper;
import edu.jjxy.studyroom.backend.service.ConfigService;
import edu.jjxy.studyroom.backend.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统配置控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final ConfigService configService;
    private final ConfigMapper configMapper;
    private final OperLogService operLogService;
    private final JwtUtil jwtUtil;

    /**
     * 获取所有配置项
     * GET /api/admin/config/list
     */
    @GetMapping("/list")
    @RequiresPermissions("config:view")
    public R<List<Config>> getConfigList() {
        List<Config> configs = configMapper.selectList(null);
        return R.ok(configs);
    }

    /**
     * 更新配置项
     * PUT /api/admin/config/update
     */
    @PutMapping("/update")
    @RequiresPermissions("config:edit")
    public R<Void> updateConfig(@Validated @RequestBody ConfigDTO dto, @RequestParam(required = false) String operIp, HttpServletRequest request) {
        Long adminId = getCurrentUserId(request);
        configService.updateConfig(adminId, dto, operIp);
        return R.ok("配置更新成功", null);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeaderName());
        if (authHeader != null && authHeader.startsWith(jwtUtil.getTokenPrefix())) {
            String token = authHeader.substring(jwtUtil.getTokenPrefix().length() + 1);
            return jwtUtil.getUserId(token);
        }
        return null;
    }
}
