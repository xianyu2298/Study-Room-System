package edu.jjxy.studyroom.backend.service;

import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Reserve;
import edu.jjxy.studyroom.backend.entity.Review;
import edu.jjxy.studyroom.backend.entity.Room;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.dto.ReviewDTO;
import edu.jjxy.studyroom.backend.entity.vo.ReviewVo;
import edu.jjxy.studyroom.backend.mapper.ReserveMapper;
import edu.jjxy.studyroom.backend.mapper.ReviewMapper;
import edu.jjxy.studyroom.backend.mapper.RoomMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import edu.jjxy.studyroom.backend.util.SensitiveWordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ReserveMapper reserveMapper;
    private final RoomMapper roomMapper;
    private final UserMapper userMapper;

    private static final String SENSITIVE_ERROR_MSG = "评价内容包含敏感词，请修改";

    /**
     * 创建评价
     */
    @Transactional(rollbackFor = Exception.class)
    public ReviewVo createReview(Long userId, ReviewDTO dto) {
        // 敏感词过滤
        if (SensitiveWordUtil.containsSensitiveWord(dto.getContent())) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, SENSITIVE_ERROR_MSG);
        }

        // 查询预约
        Reserve reserve = reserveMapper.selectById(dto.getReserveId());
        if (reserve == null) throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        if (!reserve.getUserId().equals(userId)) throw new BusinessException(ResultCode.RESERVE_NOT_YOURS);

        // 仅已完成预约可评价
        if (reserve.getStatus() != Constants.RESERVE_COMPLETED) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "仅能对已完成的预约进行评价");
        }

        // 不可重复评价
        Review existing = reviewMapper.selectByReserveId(dto.getReserveId());
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "该预约已评价，不可重复评价");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setReserveId(dto.getReserveId());
        review.setRoomId(reserve.getRoomId());
        review.setScore(dto.getScore());
        review.setContent(SensitiveWordUtil.filter(dto.getContent()));
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        log.info("创建评价 - id: {}, userId: {}, roomId: {}, score: {}", review.getId(), userId, reserve.getRoomId(), dto.getScore());
        return convertToVo(review);
    }

    /**
     * 查询我的评价列表
     */
    public List<ReviewVo> getMyReviews(Long userId) {
        return reviewMapper.selectByUserId(userId).stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    private ReviewVo convertToVo(Review r) {
        ReviewVo vo = new ReviewVo();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setReserveId(r.getReserveId());
        vo.setRoomId(r.getRoomId());
        vo.setScore(r.getScore());
        vo.setContent(r.getContent());
        vo.setCreateTime(r.getCreateTime());

        User user = userMapper.selectById(r.getUserId());
        if (user != null) vo.setUserName(user.getName());

        Room room = roomMapper.selectById(r.getRoomId());
        if (room != null) vo.setRoomName(room.getName());

        return vo;
    }
}
