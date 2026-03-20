package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.vo.ViolationVo;
import edu.jjxy.studyroom.backend.service.StudentViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生违规记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/violation")
@RequiredArgsConstructor
public class StudentViolationController {

    private final StudentViolationService studentViolationService;

    /**
     * 查询我的违规记录
     * GET /api/violation/my/list
     */
    @GetMapping("/my/list")
    @RequiresPermissions("reserve:create")
    public R<List<ViolationVo>> getMyViolations() {
        Long userId = getCurrentUserId();
        List<ViolationVo> result = studentViolationService.getMyViolations(userId);
        return R.ok(result);
    }

    private Long getCurrentUserId() {
        return (Long) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
    }
}
