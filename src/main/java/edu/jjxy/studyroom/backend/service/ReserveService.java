package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Reserve;
import edu.jjxy.studyroom.backend.entity.Room;
import edu.jjxy.studyroom.backend.entity.Seat;
import edu.jjxy.studyroom.backend.entity.dto.ReserveDTO;
import edu.jjxy.studyroom.backend.entity.vo.ReserveVo;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.mapper.ReserveMapper;
import edu.jjxy.studyroom.backend.mapper.RoomMapper;
import edu.jjxy.studyroom.backend.mapper.SeatMapper;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预约服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveService {

    private final ReserveMapper reserveMapper;
    private final RoomMapper roomMapper;
    private final SeatMapper seatMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigService configService;

    private static final String REDIS_KEY_SEAT_STATUS = "seat:status:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 学生端 ====================

    /**
     * 创建预约
     */
    @Transactional(rollbackFor = Exception.class)
    public ReserveVo createReserve(Long userId, ReserveDTO dto) {
        // 幂等性校验
        String idempotencyKey = Constants.REDIS_IDEMPOTENCY + "reserve:" + userId + ":" + dto.getSeatId();
        Boolean set = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(set)) {
            throw new BusinessException(ResultCode.IDEMPOTENT_ERROR);
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);

        // 检查禁止预约
        if (user.getBanEndTime() != null && user.getBanEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.USER_BANNED, user.getBanEndTime().format(DATETIME_FMT));
        }

        // 查询座位
        Seat seat = seatMapper.selectById(dto.getSeatId());
        if (seat == null) throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        if (seat.getStatus() == Constants.SEAT_DISABLED || seat.getStatus() == Constants.SEAT_MAINTENANCE) {
            throw new BusinessException(ResultCode.RESERVE_SEAT_UNAVAILABLE);
        }

        // 查询自习室
        Room room = roomMapper.selectById(seat.getRoomId());
        if (room == null) throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        if (!room.isConfigured()) throw new BusinessException(ResultCode.ROOM_NOT_CONFIGURED);

        // 解析时间
        LocalDate reserveDate = LocalDate.parse(dto.getReserveDate(), DATE_FMT);
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());
        LocalDateTime startDateTime = LocalDateTime.of(reserveDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(reserveDate, endTime);

        // 校验预约时间合法性
        LocalDateTime now = LocalDateTime.now();
        if (!reserveDate.equals(now.toLocalDate())) {
            throw new BusinessException(ResultCode.RESERVE_TIME_INVALID);
        }
        if (startDateTime.isBefore(now)) {
            throw new BusinessException(ResultCode.RESERVE_TIME_BEFORE_NOW);
        }
        if (startTime.isBefore(room.getOpenTime())) {
            throw new BusinessException(ResultCode.RESERVE_TIME_BEFORE_OPEN);
        }
        if (endTime.isAfter(room.getCloseTime())) {
            throw new BusinessException(ResultCode.RESERVE_TIME_AFTER_CLOSE);
        }

        // 校验预约时长
        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (minutes < 60) {
            throw new BusinessException(ResultCode.RESERVE_DURATION_INVALID, 1);
        }
        int maxHour = configService.getIntValue(Constants.CONFIG_MAX_RESERVE_HOUR, 4);
        if (minutes > maxHour * 60) {
            throw new BusinessException(ResultCode.RESERVE_DURATION_INVALID, maxHour);
        }
        if (minutes % 30 != 0) {
            throw new BusinessException(ResultCode.RESERVE_TIME_GRANULARITY_ERROR);
        }

        // 重复预约校验
        Reserve existing = reserveMapper.selectActiveByUserId(userId);
        if (existing != null) {
            throw new BusinessException(ResultCode.RESERVE_CONFLICT);
        }

        // 座位状态二次校验
        Seat freshSeat = seatMapper.selectById(dto.getSeatId());
        if (freshSeat.getStatus() != Constants.SEAT_AVAILABLE) {
            throw new BusinessException(ResultCode.RESERVE_SEAT_UNAVAILABLE);
        }

        // 生成预约
        Reserve reserve = new Reserve();
        reserve.setUserId(userId);
        reserve.setRoomId(room.getId());
        reserve.setSeatId(seat.getId());
        reserve.setStartTime(startDateTime);
        reserve.setEndTime(endDateTime);
        reserve.setStatus(Constants.RESERVE_PENDING);
        reserve.setQrCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        reserve.setCreateTime(LocalDateTime.now());
        reserve.setUpdateTime(LocalDateTime.now());
        reserveMapper.insert(reserve);

        // 更新座位状态
        Seat updateSeat = new Seat();
        updateSeat.setId(seat.getId());
        updateSeat.setStatus(Constants.SEAT_RESERVED);
        seatMapper.updateById(updateSeat);

        // 清除缓存
        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + seat.getId());
        redisTemplate.delete(Constants.REDIS_HOT_SEAT + room.getId());

        log.info("创建预约成功 - userId: {}, seatId: {}, reserveId: {}", userId, dto.getSeatId(), reserve.getId());
        return convertToVo(reserve, user, room, seat);
    }

    /**
     * 取消预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelReserve(Long userId, Long reserveId) {
        Reserve reserve = reserveMapper.selectById(reserveId);
        if (reserve == null) throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        if (!reserve.getUserId().equals(userId)) throw new BusinessException(ResultCode.RESERVE_NOT_YOURS);
        if (reserve.getStatus() == Constants.RESERVE_CANCELLED
                || reserve.getStatus() == Constants.RESERVE_COMPLETED
                || reserve.getStatus() == Constants.RESERVE_NO_SHOW) {
            throw new BusinessException(ResultCode.RESERVE_CANNOT_CANCEL);
        }

        int cancelBeforeMin = configService.getIntValue(Constants.CONFIG_CANCEL_BEFORE_MIN, 30);
        if (reserve.getStartTime().minusMinutes(cancelBeforeMin).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.RESERVE_CANCEL_TOO_LATE);
        }

        doCancel(reserve, "用户取消");
    }

    /**
     * 执行取消（内部方法）
     */
    private void doCancel(Reserve reserve, String reason) {
        reserve.setStatus(Constants.RESERVE_CANCELLED);
        reserve.setUpdateTime(LocalDateTime.now());
        reserveMapper.updateById(reserve);

        // 恢复座位状态
        Seat seat = seatMapper.selectById(reserve.getSeatId());
        if (seat != null) {
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setUpdateTime(LocalDateTime.now());
            seatMapper.updateById(seat);
        }

        redisTemplate.delete(REDIS_KEY_SEAT_STATUS + reserve.getSeatId());
        log.info("取消预约 - reserveId: {}, reason: {}", reserve.getId(), reason);
    }

    /**
     * 获取用户预约列表（分页）
     */
    public Page<ReserveVo> getMyReservePage(Long userId, Integer pageNum, Integer pageSize, Integer status) {
        Page<Reserve> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reserve> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reserve::getUserId, userId);
        if (status != null) wrapper.eq(Reserve::getStatus, status);
        wrapper.orderByDesc(Reserve::getCreateTime);
        Page<Reserve> result = reserveMapper.selectPage(page, wrapper);

        Page<ReserveVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ReserveVo> voList = result.getRecords().stream()
                .map(r -> {
                    User user = userMapper.selectById(r.getUserId());
                    Room room = roomMapper.selectById(r.getRoomId());
                    Seat seat = seatMapper.selectById(r.getSeatId());
                    return convertToVo(r, user, room, seat);
                }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取用户预约统计
     */
    public java.util.Map<String, Object> getMyStats(Long userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalReserves", reserveMapper.countTotalByUserId(userId));
        stats.put("completedReserves", reserveMapper.countCompletedByUserId(userId));
        stats.put("pendingReserves", reserveMapper.countActiveByUserId(userId));
        stats.put("todayReserves", reserveMapper.countPendingTodayByUserId(userId));
        return stats;
    }

    // ==================== 管理端 ====================

    /**
     * 分页查询预约列表（管理端）
     */
    public Page<ReserveVo> getAdminReservePage(Integer pageNum, Integer pageSize,
            Long userId, Long roomId, Integer status,
            String startDate, String endDate) {
        Page<Reserve> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Reserve> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(Reserve::getUserId, userId);
        if (roomId != null) wrapper.eq(Reserve::getRoomId, roomId);
        if (status != null) wrapper.eq(Reserve::getStatus, status);
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Reserve::getCreateTime, LocalDate.parse(startDate, DATE_FMT).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Reserve::getCreateTime, LocalDate.parse(endDate, DATE_FMT).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(Reserve::getCreateTime);
        Page<Reserve> result = reserveMapper.selectPage(page, wrapper);

        Page<ReserveVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ReserveVo> voList = result.getRecords().stream()
                .map(r -> {
                    User user = userMapper.selectById(r.getUserId());
                    Room room = roomMapper.selectById(r.getRoomId());
                    Seat seat = seatMapper.selectById(r.getSeatId());
                    return convertToVo(r, user, room, seat);
                }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 管理员取消预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void adminCancel(Long adminId, Long reserveId) {
        Reserve reserve = reserveMapper.selectById(reserveId);
        if (reserve == null) throw new BusinessException(ResultCode.RESERVE_NOT_FOUND);
        if (reserve.getStatus() != Constants.RESERVE_PENDING && reserve.getStatus() != Constants.RESERVE_IN_PROGRESS) {
            throw new BusinessException(ResultCode.RESERVE_CANNOT_CANCEL);
        }
        doCancel(reserve, "管理员取消: " + adminId);
    }

    /**
     * 获取统计数据
     */
    public java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalRooms", roomMapper.selectCount(null));
        stats.put("totalReserves", reserveMapper.selectCount(null));
        stats.put("todayReserves", reserveMapper.countTodayReserves());
        return stats;
    }

    // ==================== 工具方法 ====================

    private ReserveVo convertToVo(Reserve r, User user, Room room, Seat seat) {
        ReserveVo vo = new ReserveVo();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
            vo.setStudentNo(user.getStudentNo());
        }
        vo.setRoomId(r.getRoomId());
        if (room != null) vo.setRoomName(room.getName());
        vo.setSeatId(r.getSeatId());
        if (seat != null) vo.setSeatNo(seat.getSeatNo());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setSignTime(r.getSignTime());
        vo.setQuitTime(r.getQuitTime());
        vo.setStatus(r.getStatus());
        vo.setStatusName(getStatusName(r.getStatus()));
        vo.setQrCode(r.getQrCode());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待签到";
            case 1: return "进行中";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "爽约";
            default: return "未知";
        }
    }
}
