package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.dto.RoomDTO;
import edu.jjxy.studyroom.backend.entity.vo.RoomVo;
import edu.jjxy.studyroom.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 自习室管理控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/room")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;

    /**
     * 分页查询自习室列表（管理员）
     * GET /api/admin/room/list
     */
    @GetMapping("/list")
    @RequiresPermissions("room:view")
    public R<Page<RoomVo>> getRoomPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<RoomVo> page = roomService.getRoomPage(pageNum, pageSize, keyword, status);
        return R.ok(page);
    }

    /**
     * 创建自习室（管理员）
     * POST /api/admin/room/add
     */
    @PostMapping("/add")
    @RequiresPermissions("room:edit")
    public R<Void> createRoom(@Validated @RequestBody RoomDTO dto) {
        roomService.createRoom(dto);
        return R.ok("自习室创建成功", null);
    }

    /**
     * 更新自习室（管理员）
     * PUT /api/admin/room/update
     */
    @PutMapping("/update")
    @RequiresPermissions("room:edit")
    public R<Void> updateRoom(@Validated @RequestBody RoomDTO dto) {
        roomService.updateRoom(dto);
        return R.ok("自习室更新成功", null);
    }

    /**
     * 删除自习室（管理员）
     * DELETE /api/admin/room/{id}
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("room:delete")
    public R<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return R.ok("自习室删除成功", null);
    }

    /**
     * 切换自习室状态（管理员）
     * POST /api/admin/room/toggleStatus/{id}
     */
    @PostMapping("/toggleStatus/{id}")
    @RequiresPermissions("room:edit")
    public R<Void> toggleStatus(@PathVariable Long id) {
        roomService.toggleStatus(id);
        return R.ok("状态切换成功", null);
    }
}
