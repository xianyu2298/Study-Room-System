package edu.jjxy.studyroom.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.entity.Room;
import edu.jjxy.studyroom.backend.entity.dto.RoomDTO;
import edu.jjxy.studyroom.backend.entity.vo.RoomVo;
import edu.jjxy.studyroom.backend.mapper.RoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 自习室服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomMapper roomMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_ROOM_LIST = "room:list:";
    private static final String REDIS_KEY_HOT_ROOM = Constants.REDIS_HOT_ROOM;

    /**
     * 分页查询自习室列表（管理员）
     */
    public Page<RoomVo> getRoomPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<Room> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Room::getRoomNo, keyword)
                    .or().like(Room::getName, keyword)
                    .or().like(Room::getArea, keyword));
        }
        if (status != null) {
            wrapper.eq(Room::getStatus, status);
        }
        wrapper.orderByDesc(Room::getCreateTime);

        Page<Room> result = roomMapper.selectPage(page, wrapper);

        Page<RoomVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<RoomVo> voList = result.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 查询所有启用状态的自习室（学生端）
     */
    public List<RoomVo> getActiveRooms() {
        String cacheKey = REDIS_KEY_ROOM_LIST + "active";
        @SuppressWarnings("unchecked")
        List<RoomVo> cached = (List<RoomVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Room> rooms = roomMapper.selectActiveRooms();
        List<RoomVo> voList = rooms.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, voList, 30, TimeUnit.MINUTES);
        return voList;
    }

    /**
     * 根据ID查询自习室
     */
    public Room getById(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }
        return room;
    }

    /**
     * 根据ID查询自习室详情
     */
    public RoomVo getRoomDetail(Long id) {
        Room room = getById(id);
        return convertToVo(room);
    }

    /**
     * 查询所有区域
     */
    public List<String> getAllAreas() {
        return roomMapper.selectAllAreas();
    }

    /**
     * 创建自习室
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRoom(RoomDTO dto) {
        // 校验编号唯一性
        Room existByNo = roomMapper.selectByRoomNo(dto.getRoomNo());
        if (existByNo != null) {
            throw new BusinessException(ResultCode.ROOM_NO_EXISTS);
        }

        // 校验名称唯一性
        Room existByName = roomMapper.selectByName(dto.getName());
        if (existByName != null) {
            throw new BusinessException(ResultCode.ROOM_NAME_EXISTS);
        }

        Room room = convertToEntity(dto);
        roomMapper.insert(room);

        clearRoomCache();
        log.info("创建自习室成功 - id: {}, name: {}", room.getId(), room.getName());
    }

    /**
     * 更新自习室
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(RoomDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "自习室ID不能为空");
        }

        Room room = roomMapper.selectById(dto.getId());
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 如果修改了编号，校验唯一性
        if (!room.getRoomNo().equals(dto.getRoomNo())) {
            Room exist = roomMapper.selectByRoomNo(dto.getRoomNo());
            if (exist != null) {
                throw new BusinessException(ResultCode.ROOM_NO_EXISTS);
            }
        }

        // 如果修改了名称，校验唯一性
        if (!room.getName().equals(dto.getName())) {
            Room exist = roomMapper.selectByName(dto.getName());
            if (exist != null) {
                throw new BusinessException(ResultCode.ROOM_NAME_EXISTS);
            }
        }

        // 校验总座位数
        int currentSeatCount = roomMapper.countSeatsByRoomId(room.getId());
        if (dto.getTotalSeat() < currentSeatCount) {
            throw new BusinessException(ResultCode.ROOM_SEAT_COUNT_ERROR, currentSeatCount);
        }

        // 如果设置为禁用，先取消所有有效预约
        if (dto.getStatus() != null && dto.getStatus() == 1 && room.getStatus() == 0) {
            // TODO: 取消该自习室所有有效预约
            roomMapper.disableSeatsByRoomId(room.getId());
        }

        Room updateRoom = convertToEntity(dto);
        updateRoom.setId(room.getId());
        updateRoom.setCreateTime(room.getCreateTime());
        roomMapper.updateById(updateRoom);

        clearRoomCache();
        log.info("更新自习室成功 - id: {}, name: {}", room.getId(), updateRoom.getName());
    }

    /**
     * 删除自习室
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        // 校验是否有有效预约
        int effectiveReserveCount = roomMapper.countEffectiveReservesByRoomId(id);
        if (effectiveReserveCount > 0) {
            throw new BusinessException(ResultCode.ROOM_HAS_EFFECTIVE_RESERVE);
        }

        roomMapper.deleteById(id);
        clearRoomCache();
        log.info("删除自习室成功 - id: {}, name: {}", id, room.getName());
    }

    /**
     * 切换自习室状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id) {
        Room room = roomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND);
        }

        int newStatus = room.getStatus() == 0 ? 1 : 0;
        Room update = new Room();
        update.setId(id);
        update.setStatus(newStatus);
        roomMapper.updateById(update);

        // 如果禁用，清除该自习室的座位
        if (newStatus == 1) {
            roomMapper.disableSeatsByRoomId(id);
        }

        clearRoomCache();
        log.info("切换自习室状态 - id: {}, oldStatus: {}, newStatus: {}", id, room.getStatus(), newStatus);
    }

    /**
     * 获取热门自习室（7天预约量排序）
     */
    public List<RoomVo> getHotRooms() {
        String cacheKey = REDIS_KEY_HOT_ROOM + "7d";
        @SuppressWarnings("unchecked")
        List<RoomVo> cached = (List<RoomVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询所有启用自习室，按预约量排序
        List<Room> rooms = roomMapper.selectActiveRooms();
        List<RoomVo> voList = rooms.stream()
                .map(this::convertToVo)
                .sorted((a, b) -> {
                    Integer aCount = a.getTodayReserveCount() != null ? a.getTodayReserveCount() : 0;
                    Integer bCount = b.getTodayReserveCount() != null ? b.getTodayReserveCount() : 0;
                    return bCount.compareTo(aCount);
                })
                .limit(10)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, voList, 30, TimeUnit.MINUTES);
        return voList;
    }

    /**
     * DTO转实体
     */
    private Room convertToEntity(RoomDTO dto) {
        Room room = new Room();
        room.setRoomNo(dto.getRoomNo());
        room.setName(dto.getName());
        room.setArea(dto.getArea());

        // 解析时间字符串
        if (StringUtils.hasText(dto.getOpenTime())) {
            room.setOpenTime(LocalTime.parse(dto.getOpenTime()));
        }
        if (StringUtils.hasText(dto.getCloseTime())) {
            room.setCloseTime(LocalTime.parse(dto.getCloseTime()));
        }

        room.setTotalSeat(dto.getTotalSeat());

        // 未配置环境时默认为"插座"
        if (!StringUtils.hasText(dto.getEnvironment())) {
            room.setEnvironment(Constants.DEFAULT_ENVIRONMENT);
        } else {
            room.setEnvironment(dto.getEnvironment());
        }

        room.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.STATUS_NORMAL);
        return room;
    }

    /**
     * 实体转VO
     */
    private RoomVo convertToVo(Room room) {
        RoomVo vo = new RoomVo();
        vo.setId(room.getId());
        vo.setRoomNo(room.getRoomNo());
        vo.setName(room.getName());
        vo.setArea(room.getArea());

        if (room.getOpenTime() != null) {
            vo.setOpenTime(room.getOpenTime().toString());
        }
        if (room.getCloseTime() != null) {
            vo.setCloseTime(room.getCloseTime().toString());
        }

        vo.setTotalSeat(room.getTotalSeat());

        // 查询已添加座位数
        int seatCount = roomMapper.countSeatsByRoomId(room.getId());
        vo.setSeatCount(seatCount);

        // 查询可用座位数
        // TODO: 统计当前可预约的座位数
        vo.setAvailableSeatCount(seatCount);

        vo.setEnvironment(room.getEnvironment());
        if (StringUtils.hasText(room.getEnvironment())) {
            vo.setEnvironmentList(Arrays.asList(room.getEnvironment().split(",")));
        } else {
            vo.setEnvironmentList(new ArrayList<>());
        }

        vo.setStatus(room.getStatus());
        vo.setIsConfigured(room.isConfigured());

        // TODO: 查询今日预约数
        vo.setTodayReserveCount(0);

        return vo;
    }

    /**
     * 清除自习室缓存
     */
    private void clearRoomCache() {
        redisTemplate.delete(REDIS_KEY_ROOM_LIST + "active");
        redisTemplate.delete(REDIS_KEY_HOT_ROOM + "7d");
    }
}
