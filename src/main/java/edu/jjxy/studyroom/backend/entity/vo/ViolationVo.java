package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 违规记录 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationVo {

    private Long id;

    private Long userId;

    private String userName;

    private String studentNo;

    private Long reserveId;

    private Integer type;

    private String typeName;

    private String reason;

    private String handleResult;

    private LocalDateTime createTime;
}
