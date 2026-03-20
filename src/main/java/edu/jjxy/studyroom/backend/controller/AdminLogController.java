package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.vo.OperLogVo;
import edu.jjxy.studyroom.backend.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/log")
@RequiredArgsConstructor
public class AdminLogController {

    private final OperLogService operLogService;

    /**
     * 查询最近操作日志
     * GET /api/admin/log/list
     */
    @GetMapping("/list")
    @RequiresPermissions("log:view")
    public R<List<OperLogVo>> getLogList(@RequestParam(defaultValue = "100") Integer limit) {
        List<OperLogVo> logs = operLogService.getRecentLogs(limit);
        return R.ok(logs);
    }
}
