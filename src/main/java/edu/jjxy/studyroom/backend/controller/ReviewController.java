package edu.jjxy.studyroom.backend.controller;

import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.config.JwtUtil;
import edu.jjxy.studyroom.backend.entity.dto.ReviewDTO;
import edu.jjxy.studyroom.backend.entity.vo.ReviewVo;
import edu.jjxy.studyroom.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评价控制器（学生端）
 */
@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    /**
     * 提交评价
     * POST /api/review/create
     */
    @PostMapping("/create")
    @RequiresPermissions("reserve:create")
    public R<ReviewVo> createReview(@Validated @RequestBody ReviewDTO dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ReviewVo result = reviewService.createReview(userId, dto);
        return R.ok("评价成功", result);
    }

    /**
     * 查询我的评价列表
     * GET /api/review/my/list
     */
    @GetMapping("/my/list")
    @RequiresPermissions("reserve:create")
    public R<List<ReviewVo>> getMyReviews(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ReviewVo> result = reviewService.getMyReviews(userId);
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
