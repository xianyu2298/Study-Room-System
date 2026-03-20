package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.dto.NoticeDTO;
import edu.jjxy.studyroom.backend.entity.vo.NoticeVo;
import edu.jjxy.studyroom.backend.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 公告控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询公告列表
     * GET /api/admin/notice/list
     */
    @GetMapping("/list")
    @RequiresPermissions("notice:view")
    public R<Page<NoticeVo>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<NoticeVo> result = noticeService.getNoticePage(pageNum, pageSize, keyword);
        return R.ok(result);
    }

    /**
     * 创建公告
     * POST /api/admin/notice/add
     */
    @PostMapping("/add")
    @RequiresPermissions("notice:edit")
    public R<Void> createNotice(@Validated @RequestBody NoticeDTO dto, HttpServletRequest request) {
        Long adminId = getCurrentUserId(request);
        noticeService.createNotice(adminId, dto);
        return R.ok("公告发布成功", null);
    }

    /**
     * 更新公告
     * PUT /api/admin/notice/update
     */
    @PutMapping("/update")
    @RequiresPermissions("notice:edit")
    public R<Void> updateNotice(@Validated @RequestBody NoticeDTO dto, HttpServletRequest request) {
        Long adminId = getCurrentUserId(request);
        noticeService.updateNotice(adminId, dto);
        return R.ok("公告更新成功", null);
    }

    /**
     * 删除公告（逻辑删除）
     * DELETE /api/admin/notice/{id}
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("notice:edit")
    public R<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return R.ok("删除成功", null);
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
