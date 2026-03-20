package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVo {

    private Long id;

    private Long userId;

    private String userName;

    private Long reserveId;

    private Long roomId;

    private String roomName;

    private Integer score;

    private String content;

    private LocalDateTime createTime;
}
