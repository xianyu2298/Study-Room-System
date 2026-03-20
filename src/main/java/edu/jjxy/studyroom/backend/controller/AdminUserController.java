package edu.jjxy.studyroom.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.jjxy.studyroom.backend.common.BusinessException;
import edu.jjxy.studyroom.backend.common.Constants;
import edu.jjxy.studyroom.backend.common.ResultCode;
import edu.jjxy.studyroom.backend.common.R;
import edu.jjxy.studyroom.backend.entity.User;
import edu.jjxy.studyroom.backend.entity.dto.BatchDTO;
import edu.jjxy.studyroom.backend.entity.vo.UserVo;
import edu.jjxy.studyroom.backend.mapper.UserMapper;
import edu.jjxy.studyroom.backend.service.ConfigService;
import edu.jjxy.studyroom.backend.service.OperLogService;
import edu.jjxy.studyroom.backend.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器（管理员端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final ConfigService configService;
    private final OperLogService operLogService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询学生用户列表
     * GET /api/admin/user/list
     */
    @GetMapping("/list")
    @RequiresPermissions("user:view")
    public R<Page<UserVo>> getStudentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 仅查询学生（role=0）
        wrapper.eq(User::getRole, Constants.ROLE_STUDENT);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getName, keyword)
                    .or().like(User::getStudentNo, keyword)
                    .or().like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);

        Page<UserVo> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserVo> voList = result.getRecords().stream().map(this::convertToVo).collect(Collectors.toList());
        voPage.setRecords(voList);
        return R.ok(voPage);
    }

    /**
     * 启用/禁用学生账号
     * POST /api/admin/user/toggleStatus/{id}
     */
    @PostMapping("/toggleStatus/{id}")
    @RequiresPermissions("user:edit")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> toggleStatus(@PathVariable Long id, @RequestParam(required = false) String operIp) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (user.getRole() != Constants.ROLE_STUDENT) {
            throw new BusinessException(ResultCode.USER_NOT_STUDENT);
        }

        Long adminId = getCurrentUserId();
        int oldStatus = user.getStatus();
        int newStatus = oldStatus == Constants.STATUS_NORMAL ? Constants.STATUS_DISABLED : Constants.STATUS_NORMAL;

        if (newStatus == Constants.STATUS_DISABLED) {
            // 禁用时，先取消所有有效预约
            // TODO: 调用 reserveService 取消该用户所有待签到/进行中的预约
        }

        user.setStatus(newStatus);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        operLogService.log(adminId, Constants.LOG_MODULE_USER,
                "切换用户状态: userId=" + id + ", " + (newStatus == 0 ? "启用" : "禁用"),
                operIp != null ? operIp : "0.0.0.0");

        return R.ok((newStatus == 0 ? "启用" : "禁用") + "成功", null);
    }

    /**
     * 重置学生密码
     * POST /api/admin/user/resetPassword/{id}
     */
    @PostMapping("/resetPassword/{id}")
    @RequiresPermissions("user:edit")
    public R<Void> resetPassword(@PathVariable Long id, @RequestParam(required = false) String operIp) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (user.getRole() != Constants.ROLE_STUDENT) {
            throw new BusinessException(ResultCode.USER_NOT_STUDENT);
        }

        // 重置为默认密码 123456
        String encryptedPassword = PasswordUtil.encryptPassword(Constants.DEFAULT_PASSWORD);
        user.setPassword(encryptedPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        Long adminId = getCurrentUserId();
        operLogService.log(adminId, Constants.LOG_MODULE_USER,
                "重置用户密码: userId=" + id,
                operIp != null ? operIp : "0.0.0.0");

        // TODO: 发送邮件通知学生
        return R.ok("密码已重置为123456", null);
    }

    /**
     * 批量启用/禁用
     * POST /api/admin/user/batchToggle
     */
    @PostMapping("/batchToggle")
    @RequiresPermissions("user:edit")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> batchToggle(@RequestBody BatchDTO dto, @RequestParam Integer status, @RequestParam(required = false) String operIp) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BusinessException(ResultCode.PARAMETER_ERROR, "请选择要操作的用户");
        }
        if (dto.getIds().size() > Constants.BATCH_MAX_SIZE) {
            throw new BusinessException(ResultCode.BATCH_MAX_LIMIT, Constants.BATCH_MAX_SIZE);
        }

        Long adminId = getCurrentUserId();
        for (Long userId : dto.getIds()) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getRole() == Constants.ROLE_STUDENT) {
                user.setStatus(status);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        operLogService.log(adminId, Constants.LOG_MODULE_USER,
                "批量切换用户状态: ids=" + dto.getIds() + ", status=" + status,
                operIp != null ? operIp : "0.0.0.0");

        return R.ok("批量操作成功", null);
    }

    private UserVo convertToVo(User user) {
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setStudentNo(user.getStudentNo());
        vo.setEmail(user.getEmail());
        vo.setName(user.getName());
        vo.setRole(user.getRole());
        vo.setRoleName(getRoleName(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setStatusName(getStatusName(user.getStatus()));
        vo.setBanEndTime(user.getBanEndTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private String getRoleName(Integer role) {
        if (role == null) return "";
        switch (role) {
            case Constants.ROLE_STUDENT: return "学生";
            case Constants.ROLE_ADMIN: return "管理员";
            case Constants.ROLE_SUPER_ADMIN: return "超级管理员";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case Constants.STATUS_NORMAL: return "正常";
            case Constants.STATUS_DISABLED: return "禁用";
            case Constants.STATUS_LOCKED: return "锁定";
            default: return "未知";
        }
    }

    private Long getCurrentUserId() {
        return (Long) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
    }
}
