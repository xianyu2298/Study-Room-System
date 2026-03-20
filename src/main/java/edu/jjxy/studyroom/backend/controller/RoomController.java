package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.dto.RoomDTO;
import edu.jjxy.studyroom.backend.entity.vo.RoomVo;
import edu.jjxy.studyroom.backend.entity.vo.SeatVo;
import edu.jjxy.studyroom.backend.service.RoomService;
import edu.jjxy.studyroom.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自习室控制器（学生端 + 管理端查询）
 */
@Slf4j
@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SeatService seatService;

    /**
     * 获取自习室列表（学生端）
     * GET /api/room/list
     */
    @GetMapping("/list")
    public R<List<RoomVo>> getRoomList() {
        List<RoomVo> rooms = roomService.getActiveRooms();
        return R.ok(rooms);
    }

    /**
     * 获取自习室详情（学生端）
     * GET /api/room/{id}
     */
    @GetMapping("/{id}")
    public R<RoomVo> getRoomDetail(@PathVariable Long id) {
        RoomVo room = roomService.getRoomDetail(id);
        return R.ok(room);
    }

    /**
     * 获取自习室的座位列表（学生端 - 仅有效座位）
     * GET /api/room/{roomId}/seats
     */
    @GetMapping("/{roomId}/seats")
    @RequiresPermissions("reserve:create")
    public R<List<SeatVo>> getRoomSeats(@PathVariable Long roomId) {
        List<SeatVo> seats = seatService.getAvailableSeats(roomId);
        return R.ok(seats);
    }

    /**
     * 获取所有区域
     * GET /api/room/areas
     */
    @GetMapping("/areas")
    public R<List<String>> getAreas() {
        List<String> areas = roomService.getAllAreas();
        return R.ok(areas);
    }

    /**
     * 获取热门自习室
     * GET /api/room/hot
     */
    @GetMapping("/hot")
    public R<List<RoomVo>> getHotRooms() {
        List<RoomVo> rooms = roomService.getHotRooms();
        return R.ok(rooms);
    }
}
