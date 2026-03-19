package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.dto.BatchSeatDTO;
import edu.jjxy.studyroom.backend.entity.dto.SeatDTO;
import edu.jjxy.studyroom.backend.entity.vo.SeatVo;
import edu.jjxy.studyroom.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 座位控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 分页查询座位列表（管理员）
     * GET /api/admin/seat/list
     */
    @GetMapping("/list")
    @RequiresPermissions("seat:view")
    public R<Page<SeatVo>> getSeatPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<SeatVo> page = seatService.getSeatPage(pageNum, pageSize, roomId, keyword, status);
        return R.ok(page);
    }

    /**
     * 查询指定自习室的所有座位（管理员）
     * GET /api/admin/seat/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    @RequiresPermissions("seat:view")
    public R<List<SeatVo>> getAllSeatsByRoom(@PathVariable Long roomId) {
        List<SeatVo> seats = seatService.getAllSeatsByRoom(roomId);
        return R.ok(seats);
    }

    /**
     * 创建座位（管理员）
     * POST /api/admin/seat/add
     */
    @PostMapping("/add")
    @RequiresPermissions("seat:edit")
    public R<Void> createSeat(@Validated @RequestBody SeatDTO dto) {
        seatService.createSeat(dto);
        return R.ok("座位创建成功", null);
    }

    /**
     * 批量创建座位（管理员）
     * POST /api/admin/seat/batch
     */
    @PostMapping("/batch")
    @RequiresPermissions("seat:edit")
    public R<Void> batchCreateSeats(@Validated @RequestBody BatchSeatDTO dto) {
        seatService.batchCreateSeats(dto);
        return R.ok("批量创建座位成功", null);
    }

    /**
     * 更新座位（管理员）
     * PUT /api/admin/seat/update
     */
    @PutMapping("/update")
    @RequiresPermissions("seat:edit")
    public R<Void> updateSeat(@Validated @RequestBody SeatDTO dto) {
        seatService.updateSeat(dto);
        return R.ok("座位更新成功", null);
    }

    /**
     * 删除座位（管理员）
     * DELETE /api/admin/seat/{id}
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("seat:delete")
    public R<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return R.ok("座位删除成功", null);
    }

    /**
     * 切换座位状态（管理员）
     * POST /api/admin/seat/toggleStatus/{id}
     */
    @PostMapping("/toggleStatus/{id}")
    @RequiresPermissions("seat:edit")
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        seatService.toggleStatus(id, status);
        return R.ok("状态切换成功", null);
    }
}
