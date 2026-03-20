package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.dto.ReserveDTO;
import edu.jjxy.studyroom.backend.entity.vo.ReserveVo;
import edu.jjxy.studyroom.backend.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预约控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserveService reserveService;

    /**
     * 创建预约
     * POST /api/reserve/create
     */
    @PostMapping("/create")
    @RequiresPermissions("reserve:create")
    public R<ReserveVo> createReserve(@Validated @RequestBody ReserveDTO dto) {
        Long userId = getCurrentUserId();
        ReserveVo result = reserveService.createReserve(userId, dto);
        return R.ok("预约成功", result);
    }

    /**
     * 取消预约
     * POST /api/reserve/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    @RequiresPermissions("reserve:create")
    public R<Void> cancelReserve(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        reserveService.cancelReserve(userId, id);
        return R.ok("取消成功", null);
    }

    /**
     * 获取我的预约列表（分页）
     * GET /api/reserve/my/list
     */
    @GetMapping("/my/list")
    @RequiresPermissions("reserve:create")
    public R<Page<ReserveVo>> getMyReserveList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        Page<ReserveVo> result = reserveService.getMyReservePage(userId, page, size, status);
        return R.ok(result);
    }

    /**
     * 获取我的预约统计
     * GET /api/reserve/my/stats
     */
    @GetMapping("/my/stats")
    @RequiresPermissions("reserve:create")
    public R<Map<String, Object>> getMyStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = reserveService.getMyStats(userId);
        return R.ok(stats);
    }

    private Long getCurrentUserId() {
        // 从 Shiro 上下文获取当前用户ID
        return (Long) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
    }
}
