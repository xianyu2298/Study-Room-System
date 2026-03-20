package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperLogVo {

    private Long id;

    private Long operId;

    private String operName;

    private String operModule;

    private String operContent;

    private String operIp;

    private LocalDateTime operTime;
}
