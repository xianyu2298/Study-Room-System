package edu.jjxy.studyroom.backend.service;

import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Reserve;
import edu.jjxy.studyroom.backend.entity.Seat;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.Violation;
import edu.jjxy.studyroom.backend.entity.vo.ReserveVo;
import edu.jjxy.studyroom.backend.mapper.ReserveMapper;
import edu.jjxy.studyroom.backend.mapper.RoomMapper;
import edu.jjxy.studyroom.backend.mapper.SeatMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import edu.jjxy.studyroom.backend.mapper.ViolationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 签到签退服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private final ReserveMapper reserveMapper;
    private final SeatMapper seatMapper;
    private final UserMapper userMapper;
    private final ViolationMapper violationMapper;
    private final ConfigService configService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_SEAT_STATUS = "seat:status:";

    /**
     * 签到
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signIn(Long userId, Long reserveId) {
        Reserve reserve = reserveMapper.selectById(reserveId);
        if (reserve == null) throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        if (!reserve.getUserId().equals(userId)) throw new BusinessException(ResultCode.RESERVE_NOT_YOURS);
        if (reserve.getStatus() != Constants.RESERVE_PENDING) {
            throw new BusinessException(ResultCode.SIGN_STATUS_ERROR);
        }

        // 校验签到时间窗口
        int signTimeoutMin = configService.getIntValue(Constants.CONFIG_SIGN_TIMEOUT_MIN, 15);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime signDeadline = reserve.getStartTime().plusMinutes(signTimeoutMin);

        if (now.isAfter(signDeadline)) {
            // 超时 -> 爽约
            doNoShow(reserve, Constants.VIOLATION_SIGN_TIMEOUT, "签到超时");
            throw new BusinessException(ResultCode.SIGN_TIMEOUT);
        }

        // 执行签到
        reserve.setStatus(Constants.RESERVE_IN_PROGRESS);
        reserve.setSignTime(now);
        reserve.setUpdateTime(now);
        reserveMapper.updateById(reserve);

        // 更新座位状态为已占用
        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_OCCUPIED);
            seat.setUpdateTime(now);
            seatMapper.updateById(seat);
        }

        // 清除座位缓存
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());

        log.info("签到成功 - reserveId: {}, userId: {}", reserveId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "签到成功");
        result.put("signTime", now);
        result.put("endTime", reserve.getEndTime());
        return result;
    }

    /**
     * 签退
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signOut(Long userId, Long reserveId) {
        Reserve reserve = reserveMapper.selectById(reserveId);
        if (reserve == null) throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        if (!reserve.getUserId().equals(userId)) throw new BusinessException(ResultCode.RESERVE_NOT_YOURS);
        if (reserve.getStatus() != Constants.RESERVE_IN_PROGRESS) {
            throw new BusinessException(ResultCode.SIGN_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();

        // 仅在预约时间段内可签退
        if (now.isBefore(reserve.getStartTime())) {
            throw new BusinessException(ResultCode.SIGN_OUT_TIME_ERROR);
        }
        if (now.isAfter(reserve.getEndTime())) {
            // 已过结束时间 -> 自动签退（正常结束）
            doComplete(reserve, now);
            log.info("预约已超时，自动签退 - reserveId: {}", reserveId);
            Map<String, Object> timeoutResult = new HashMap<>();
            timeoutResult.put("message", "签退成功（已超时）");
            timeoutResult.put("quitTime", now);
            return timeoutResult;
        }

        // 正常签退
        doSignOut(reserve, now);

        log.info("签退成功 - reserveId: {}, userId: {}", reserveId, userId);
        Duration used = Duration.between(reserve.getSignTime(), now);
        Map<String, Object> signOutResult = new HashMap<>();
        signOutResult.put("message", "签退成功");
        signOutResult.put("quitTime", now);
        signOutResult.put("usedMinutes", used.toMinutes());
        return signOutResult;
    }

    /**
     * 执行签退
     */
    private void doSignOut(Reserve reserve, LocalDateTime quitTime) {
        reserve.setStatus(Constants.RESERVE_COMPLETED);
        reserve.setQuitTime(quitTime);
        reserve.setUpdateTime(quitTime);
        reserveMapper.updateById(reserve);

        // 恢复座位状态为可预约
        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setUpdateTime(quitTime);
            seatMapper.updateById(seat);
        }

        // 清除缓存
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());
    }

    /**
     * 执行完成（自动签退或正常结束）
     */
    private void doComplete(Reserve reserve, LocalDateTime quitTime) {
        reserve.setStatus(Constants.RESERVE_COMPLETED);
        reserve.setQuitTime(quitTime);
        reserve.setUpdateTime(quitTime);
        reserveMapper.updateById(reserve);

        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setUpdateTime(quitTime);
            seatMapper.updateById(seat);
        }
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());
    }

    /**
     * 爽约处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void doNoShow(Reserve reserve, Integer violationType, String reason) {
        LocalDateTime now = LocalDateTime.now();

        // 更新预约状态为爽约
        reserve.setStatus(Constants.RESERVE_NO_SHOW);
        reserve.setUpdateTime(now);
        reserveMapper.updateById(reserve);

        // 恢复座位状态
        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setUpdateTime(now);
            seatMapper.updateById(seat);
        }
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());

        // 记录违规
        Violation violation = new Violation();
        violation.setUserId(reserve.getUserId());
        violation.setReserveId(reserve.getId());
        violation.setType(violationType);
        violation.setReason(reason);
        violation.setCreateTime(now);
        violation.setUpdateTime(now);
        violationMapper.insert(violation);

        // 爽约惩罚：检查是否达到禁止预约次数
        int breakRuleTimes = configService.getIntValue(Constants.CONFIG_BREAK_RULE_TIMES, 3);
        int banDays = configService.getIntValue(Constants.CONFIG_BAN_DAYS, 1);

        int noShowCount = violationMapper.countByUserIdAndType(reserve.getUserId(), Constants.VIOLATION_NO_SHOW);
        if (noShowCount >= breakRuleTimes) {
            User user = userMapper.selectById(reserve.getUserId());
            if (user != null && (user.getBanEndTime() == null || user.getBanEndTime().isBefore(now))) {
                user.setBanEndTime(now.plusDays(banDays));
                user.setUpdateTime(now);
                userMapper.updateById(user);
                log.info("用户被禁止预约 - userId: {}, until: {}", reserve.getUserId(), user.getBanEndTime());
            }
        }

        log.info("爽约记录 - reserveId: {}, userId: {}, violationId: {}", reserve.getId(), reserve.getUserId(), violation.getId());
    }

    /**
     * 占座违规处理（签到后超时未签退）
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleOccupationViolation(Reserve reserve) {
        LocalDateTime now = LocalDateTime.now();

        // 标记为已完成
        reserve.setStatus(Constants.RESERVE_COMPLETED);
        reserve.setQuitTime(reserve.getEndTime());
        reserve.setUpdateTime(now);
        reserveMapper.updateById(reserve);

        // 恢复座位
        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setUpdateTime(now);
            seatMapper.updateById(seat);
        }
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());

        // 记录占座违规
        Violation violation = new Violation();
        violation.setUserId(reserve.getUserId());
        violation.setReserveId(reserve.getId());
        violation.setType(Constants.VIOLATION_OCCUPYING);
        violation.setReason("签到后超时未签退，占座违规");
        violation.setCreateTime(now);
        violation.setUpdateTime(now);
        violationMapper.insert(violation);

        // 爽约惩罚
        int breakRuleTimes = configService.getIntValue(Constants.CONFIG_BREAK_RULE_TIMES, 3);
        int banDays = configService.getIntValue(Constants.CONFIG_BAN_DAYS, 1);
        int noShowCount = violationMapper.countByUserIdAndType(reserve.getUserId(), Constants.VIOLATION_NO_SHOW);
        if (noShowCount >= breakRuleTimes) {
            User user = userMapper.selectById(reserve.getUserId());
            if (user != null) {
                user.setBanEndTime(now.plusDays(banDays));
                user.setUpdateTime(now);
                userMapper.updateById(user);
            }
        }

        log.info("占座违规记录 - reserveId: {}, userId: {}", reserve.getId(), reserve.getUserId());
    }
}
