package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.vo.ViolationVo;
import edu.jjxy.studyroom.backend.service.StudentViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;

    /**
     * 查询我的违规记录
     * GET /api/violation/my/list
     */
    @GetMapping("/my/list")
    @RequiresPermissions("reserve:create")
    public R<List<ViolationVo>> getMyViolations(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ViolationVo> result = studentViolationService.getMyViolations(userId);
        return R.ok(result);
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
