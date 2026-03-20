package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.vo.NoticeVo;
import edu.jjxy.studyroom.backend.entity.vo.ReserveVo;
import edu.jjxy.studyroom.backend.service.NoticeService;
import edu.jjxy.studyroom.backend.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预约控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/reserve")
@RequiredArgsConstructor
public class AdminReserveController {

    private final ReserveService reserveService;
    private final NoticeService noticeService;

    /**
     * 分页查询预约列表
     * GET /api/admin/reserve/list
     */
    @GetMapping("/list")
    @RequiresPermissions("reserve:view")
    public R<Page<ReserveVo>> getReserveList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<ReserveVo> result = reserveService.getAdminReservePage(
                pageNum, pageSize, userId, roomId, status, startDate, endDate);
        return R.ok(result);
    }

    /**
     * 管理员取消预约
     * POST /api/admin/reserve/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    @RequiresPermissions("reserve:manage")
    public R<Void> cancelReserve(@PathVariable Long id) {
        Long adminId = getCurrentUserId();
        reserveService.adminCancel(adminId, id);
        return R.ok("取消成功", null);
    }

    /**
     * 获取统计数据
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    @RequiresPermissions("statistics:view")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = reserveService.getStats();
        return R.ok(stats);
    }

    private Long getCurrentUserId() {
        return (Long) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
    }
}
