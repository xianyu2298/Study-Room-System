package edu.jjxy.studyroom.backend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 预约记录 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveVo {

    private Long id;

    private Long userId;

    private String userName;

    private String studentNo;

    private Long roomId;

    private String roomName;

    private Long seatId;

    private String seatNo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime signTime;

    private LocalDateTime quitTime;

    private Integer status;

    private String statusName;

    private String qrCode;

    private LocalDateTime createTime;
}
