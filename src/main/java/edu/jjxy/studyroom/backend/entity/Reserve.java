package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预约记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("reserve")
public class Reserve {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long roomId;

    private Long seatId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime signTime;

    private LocalDateTime quitTime;

    private Integer status;

    private String qrCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public boolean isPending() {
        return status != null && status == 0;
    }

    public boolean isInProgress() {
        return status != null && status == 1;
    }

    public boolean isCompleted() {
        return status != null && status == 2;
    }

    public boolean isCancelled() {
        return status != null && status == 3;
    }

    public boolean isNoShow() {
        return status != null && status == 4;
    }

    public boolean isActive() {
        return status != null && (status == 0 || status == 1);
    }
}
