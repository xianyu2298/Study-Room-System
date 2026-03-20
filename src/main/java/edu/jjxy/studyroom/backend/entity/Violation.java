package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 违规记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("violation")
public class Violation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long reserveId;

    private Integer type;

    private String reason;

    private String handleResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
