package edu.jjxy.studyroom.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体类（仅INSERT，禁止修改/删除）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("oper_log")
public class OperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operId;

    private String operModule;

    private String operContent;

    private String operIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operTime;
}
