package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeVo {

    private Long id;

    private String title;

    private String content;

    private Integer isTop;

    private String isTopName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long createBy;

    private String createByName;

    private LocalDateTime createTime;
}
