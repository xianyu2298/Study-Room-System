package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 座位实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("seat")
public class Seat {

    /**
     * 座位ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属自习室ID
     */
    private Long roomId;

    /**
     * 座位号（如01、A02）
     */
    private String seatNo;

    /**
     * 状态：0=可预约、1=已预约、2=已占用、3=禁用、4=维护中
     */
    private Integer status;

    /**
     * 是否热门：0=否、1=是
     */
    private Integer isHot;

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
     * 是否可预约
     */
    public boolean isAvailable() {
        return this.status != null && this.status == 0;
    }

    /**
     * 是否已预约
     */
    public boolean isReserved() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否已占用
     */
    public boolean isOccupied() {
        return this.status != null && this.status == 2;
    }

    /**
     * 是否有效（可预约/已预约/已占用）
     */
    public boolean isActive() {
        return this.status != null && (this.status == 0 || this.status == 1 || this.status == 2);
    }

    /**
     * 是否禁用或维护中
     */
    public boolean isInactive() {
        return this.status != null && (this.status == 3 || this.status == 4);
    }
}
