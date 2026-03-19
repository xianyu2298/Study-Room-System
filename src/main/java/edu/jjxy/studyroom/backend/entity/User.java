package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学号（学生）/管理员账号，role=0时非空
     */
    private String studentNo;

    /**
     * 注册邮箱
     */
    private String email;

    /**
     * 密码（加密后）
     */
    private String password;

    /**
     * 姓名/昵称
     */
    private String name;

    /**
     * 角色：0=学生、1=普通管理员、99=超级管理员
     */
    private Integer role;

    /**
     * 状态：0=正常、1=禁用、2=锁定
     */
    private Integer status;

    /**
     * 禁止预约结束时间
     */
    private LocalDateTime banEndTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否为学生
     */
    public boolean isStudent() {
        return this.role != null && this.role == 0;
    }

    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return this.role != null && this.role >= 1;
    }

    /**
     * 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this.role != null && this.role == 99;
    }

    /**
     * 账号是否正常
     */
    public boolean isNormal() {
        return this.status != null && this.status == 0;
    }

    /**
     * 账号是否被禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 账号是否被锁定
     */
    public boolean isLocked() {
        return this.status != null && this.status == 2;
    }

    /**
     * 是否被禁止预约
     */
    public boolean isBanned() {
        return this.banEndTime != null && this.banEndTime.isAfter(LocalDateTime.now());
    }
}
