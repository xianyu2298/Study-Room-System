package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Room;
import edu.jjxy.studyroom.backend.entity.Seat;
import edu.jjxy.studyroom.backend.entity.dto.BatchSeatDTO;
import edu.jjxy.studyroom.backend.entity.dto.SeatDTO;
import edu.jjxy.studyroom.backend.entity.vo.SeatVo;
import edu.jjxy.studyroom.backend.mapper.RoomMapper;
import edu.jjxy.studyroom.backend.mapper.SeatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 座位服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatMapper seatMapper;
    private final RoomMapper roomMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_SEAT_LIST = "seat:list:";

    /**
     * 分页查询座位列表（管理员）
     */
    public Page<SeatVo> getSeatPage(Integer pageNum, Integer pageSize, Long roomId, String keyword, Integer status) {
        Page<Seat> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();

        if (roomId != null) {
            wrapper.eq(Seat::getRoomId, roomId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Seat::getSeatNo, keyword);
        }
        if (status != null) {
            wrapper.eq(Seat::getStatus, status);
        }
        wrapper.orderByAsc(Seat::getSeatNo);

        Page<Seat> result = seatMapper.selectPage(page, wrapper);

        Page<SeatVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SeatVo> voList = result.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 查询指定自习室的有效座位列表（学生端）
     */
    public List<SeatVo> getAvailableSeats(Long roomId) {
        String cacheKey = REDIS_KEY_SEAT_LIST + roomId + ":available";
        @SuppressWarnings("unchecked")
        List<SeatVo> cached = (List<SeatVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Seat> seats = seatMapper.selectActiveSeatsByRoomId(roomId);
        List<SeatVo> voList = seats.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, voList, 30, TimeUnit.MINUTES);
        return voList;
    }

    /**
     * 查询指定自习室的所有座位（管理员）
     */
    public List<SeatVo> getAllSeatsByRoom(Long roomId) {
        String cacheKey = REDIS_KEY_SEAT_LIST + roomId + ":all";
        @SuppressWarnings("unchecked")
        List<SeatVo> cached = (List<SeatVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Seat> seats = seatMapper.selectAllSeatsByRoomId(roomId);
        List<SeatVo> voList = seats.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, voList, 30, TimeUnit.MINUTES);
        return voList;
    }

    /**
     * 根据ID查询座位
     */
    public Seat getById(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }
        return seat;
    }

    /**
     * 创建座位
     */
    @Transactional(rollbackFor = Exception.class)
    public void createSeat(SeatDTO dto) {
        // 验证自习室存在
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 检查座位号是否已存在
        Seat existSeat = seatMapper.selectByRoomIdAndSeatNo(dto.getRoomId(), dto.getSeatNo());
        if (existSeat != null) {
            throw new BusinessException(ResultCode.SEAT_NO_EXISTS);
        }

        // 验证座位数量
        int currentSeatCount = seatMapper.countByRoomId(dto.getRoomId());
        if (currentSeatCount >= room.getTotalSeat()) {
            throw new BusinessException(ResultCode.SEAT_COUNT_EXCEED, room.getTotalSeat(), currentSeatCount);
        }

        Seat seat = convertToEntity(dto);
        seatMapper.insert(seat);

        clearSeatCache(dto.getRoomId());
        log.info("创建座位成功 - id: {}, roomId: {}, seatNo: {}", seat.getId(), seat.getRoomId(), seat.getSeatNo());
    }

    /**
     * 批量创建座位
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateSeats(BatchSeatDTO dto) {
        Room room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 生成座位号列表
        List<String> seatNos = generateSeatNos(dto.getStartSeatNo(), dto.getEndSeatNo());
        if (seatNos.size() > Constants.BATCH_SEAT_MAX_SIZE) {
            throw new BusinessException(ResultCode.SEAT_BATCH_MAX_LIMIT, "100");
        }

        // 验证座位总数
        int currentCount = seatMapper.countByRoomId(dto.getRoomId());
        int newTotal = currentCount + seatNos.size();
        if (newTotal > room.getTotalSeat()) {
            throw new BusinessException(ResultCode.SEAT_COUNT_EXCEED, room.getTotalSeat(), currentCount);
        }

        // 批量插入
        List<Seat> seats = new ArrayList<>();
        for (String seatNo : seatNos) {
            Seat seat = new Seat();
            seat.setRoomId(dto.getRoomId());
            seat.setSeatNo(seatNo);
            seat.setStatus(Constants.SEAT_AVAILABLE);
            seat.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
            seats.add(seat);
        }

        for (Seat seat : seats) {
            seatMapper.insert(seat);
        }

        clearSeatCache(dto.getRoomId());
        log.info("批量创建座位成功 - roomId: {}, count: {}", dto.getRoomId(), seatNos.size());
    }

    /**
     * 更新座位
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSeat(SeatDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "座位ID不能为空");
        }

        Seat seat = seatMapper.selectById(dto.getId());
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }

        // 如果修改了座位号，验证唯一性
        if (!seat.getSeatNo().equals(dto.getSeatNo())) {
            Seat existSeat = seatMapper.selectByRoomIdAndSeatNo(seat.getRoomId(), dto.getSeatNo());
            if (existSeat != null) {
                throw new BusinessException(ResultCode.SEAT_NO_EXISTS);
            }
        }

        // 禁用或维护座位时清空有效预约
        if (dto.getStatus() != null && (dto.getStatus() == 3 || dto.getStatus() == 4)) {
            if (seat.getStatus() == 0 || seat.getStatus() == 1) {
                // TODO: 清理该座位的有效预约
            }
        }

        seat.setSeatNo(dto.getSeatNo());
        if (dto.getStatus() != null) {
            seat.setStatus(dto.getStatus());
        }
        if (dto.getIsHot() != null) {
            seat.setIsHot(dto.getIsHot());
        }
        seatMapper.updateById(seat);

        clearSeatCache(seat.getRoomId());
        log.info("更新座位成功 - id: {}, seatNo: {}", seat.getId(), seat.getSeatNo());
    }

    /**
     * 删除座位
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeat(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }

        // 验证无有效预约
        // TODO: 检查是否有有效预约

        seatMapper.deleteById(id);
        clearSeatCache(seat.getRoomId());
        log.info("删除座位成功 - id: {}, seatNo: {}", id, seat.getSeatNo());
    }

    /**
     * 切换座位状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer newStatus) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException(ResultCode.SEAT_NOT_FOUND);
        }

        if (newStatus < 0 || newStatus > 4) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "无效的座位状态");
        }

        seat.setStatus(newStatus);
        seatMapper.updateById(seat);

        clearSeatCache(seat.getRoomId());
        log.info("切换座位状态 - id: {}, newStatus: {}", id, newStatus);
    }

    /**
     * 根据座位号格式生成座位号列表
     */
    private List<String> generateSeatNos(String startSeatNo, String endSeatNo) {
        List<String> seatNos = new ArrayList<>();
        if (StringUtils.hasText(startSeatNo) && StringUtils.hasText(endSeatNo)) {
            Pattern pattern = Pattern.compile("^([A-Za-z]*)([0-9]+)$");
            Matcher startMatcher = pattern.matcher(startSeatNo);
            Matcher endMatcher = pattern.matcher(endSeatNo);
            if (startMatcher.matches() && endMatcher.matches()) {
                String startLetter = startMatcher.group(1);
                String startNum = startMatcher.group(2);
                String endLetter = endMatcher.group(1);
                String endNum = endMatcher.group(2);
                if (startLetter.equals(endLetter)) {
                    int start = Integer.parseInt(startNum);
                    int end = Integer.parseInt(endNum);
                    for (int i = start; i <= end; i++) {
                        String numStr = String.valueOf(i);
                        // 补齐为2位数字
                        if (numStr.length() < startNum.length()) {
                            numStr = String.format("%0" + startNum.length() + "d", i);
                        }
                        seatNos.add(startLetter + numStr);
                    }
                }
            }
        }
        return seatNos;
    }

    /**
     * DTO转实体
     */
    private Seat convertToEntity(SeatDTO dto) {
        Seat seat = new Seat();
        seat.setRoomId(dto.getRoomId());
        seat.setSeatNo(dto.getSeatNo());
        seat.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.SEAT_AVAILABLE);
        seat.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
        return seat;
    }

    /**
     * 实体转VO
     */
    private SeatVo convertToVo(Seat seat) {
        SeatVo vo = new SeatVo();
        vo.setId(seat.getId());
        vo.setRoomId(seat.getRoomId());
        vo.setSeatNo(seat.getSeatNo());
        vo.setStatus(seat.getStatus());
        vo.setIsHot(seat.getIsHot());
        vo.setAvailable(seat.isAvailable());
        vo.setWeekReserveCount(0);

        // 获取自习室名称
        Room room = roomMapper.selectById(seat.getRoomId());
        if (room != null) {
            vo.setRoomName(room.getName());
        }

        // 状态名称
        vo.setStatusName(getStatusName(seat.getStatus()));

        return vo;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "可预约";
            case 1: return "已预约";
            case 2: return "已占用";
            case 3: return "禁用";
            case 4: return "维护中";
            default: return "未知";
        }
    }

    /**
     * 清除座位缓存
     */
    private void clearSeatCache(Long roomId) {
        redisTemplate.delete(REDIS_KEY_SEAT_LIST + roomId + ":available");
        redisTemplate.delete(REDIS_KEY_SEAT_LIST + roomId + ":all");
    }
}
