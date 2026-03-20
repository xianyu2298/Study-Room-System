package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.vo.ViolationVo;
import edu.jjxy.studyroom.backend.service.ViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 违规记录控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/violation")
@RequiredArgsConstructor
public class AdminViolationController {

    private final ViolationService violationService;

    /**
     * 分页查询违规记录
     * GET /api/admin/violation/list
     */
    @GetMapping("/list")
    @RequiresPermissions("violation:view")
    public R<Page<ViolationVo>> getViolationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type) {
        Page<ViolationVo> result = violationService.getViolationPage(pageNum, pageSize, userId, type);
        return R.ok(result);
    }

    /**
     * 标记处理结果
     * POST /api/admin/violation/handle/{id}
     */
    @PostMapping("/handle/{id}")
    @RequiresPermissions("violation:manage")
    public R<Void> handleViolation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String handleResult = body.get("handleResult");
        violationService.handleViolation(id, handleResult);
        return R.ok("处理成功", null);
    }
}
