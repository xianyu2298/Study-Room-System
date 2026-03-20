package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.dto.ReserveDTO;
import edu.jjxy.studyroom.backend.entity.vo.ReserveVo;
import edu.jjxy.studyroom.backend.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;

    /**
     * 创建预约
     * POST /api/reserve/create
     */
    @PostMapping("/create")
    @RequiresPermissions("reserve:create")
    public R<ReserveVo> createReserve(@Validated @RequestBody ReserveDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ReserveVo result = reserveService.createReserve(userId, dto);
        return R.ok("预约成功", result);
    }

    /**
     * 取消预约
     * POST /api/reserve/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    @RequiresPermissions("reserve:create")
    public R<Void> cancelReserve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
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
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Page<ReserveVo> result = reserveService.getMyReservePage(userId, page, size, status);
        return R.ok(result);
    }

    /**
     * 获取我的预约统计
     * GET /api/reserve/my/stats
     */
    @GetMapping("/my/stats")
    @RequiresPermissions("reserve:create")
    public R<Map<String, Object>> getMyStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> stats = reserveService.getMyStats(userId);
        return R.ok(stats);
    }

    /**
     * 从请求头直接解析 JWT Token 获取当前用户ID
     * 不依赖 Shiro Subject.getPrincipal()
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeaderName());
        if (authHeader != null && authHeader.startsWith(jwtUtil.getTokenPrefix())) {
            String token = authHeader.substring(jwtUtil.getTokenPrefix().length() + 1);
            return jwtUtil.getUserId(token);
        }
        return null;
    }
}
