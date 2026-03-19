package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room")
public class Room {

    /**
     * 自习室ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 自习室编号（如1教101）
     */
    private String roomNo;

    /**
     * 自习室名称
     */
    private String name;

    /**
     * 所属区域（1教1楼）
     */
    private String area;

    /**
     * 每日开放时间
     */
    private LocalTime openTime;

    /**
     * 每日关闭时间
     */
    private LocalTime closeTime;

    /**
     * 总座位数
     */
    private Integer totalSeat;

    /**
     * 环境配置（空调,插座），未配置时默认「插座」
     */
    private String environment;

    /**
     * 状态：0=正常、1=禁用
     */
    private Integer status;

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
     * 是否正常
     */
    public boolean isNormal() {
        return this.status != null && this.status == 0;
    }

    /**
     * 是否已禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否已配置完成（开放时间、关闭时间、总座位数都非空）
     */
    public boolean isConfigured() {
        return this.openTime != null && this.closeTime != null && this.totalSeat != null && this.totalSeat > 0;
    }
}
