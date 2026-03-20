package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.vo.NoticeVo;
import edu.jjxy.studyroom.backend.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 获取生效公告列表
     * GET /api/notice/list
     */
    @GetMapping("/list")
    @RequiresPermissions("notice:view")
    public R<List<NoticeVo>> getActiveNotices() {
        List<NoticeVo> notices = noticeService.getActiveNotices();
        return R.ok(notices);
    }
}
