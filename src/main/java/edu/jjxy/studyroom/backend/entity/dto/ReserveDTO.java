package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * 创建预约 DTO（学生端）
 */
@Data
public class ReserveDTO {

    @NotNull(message = "座位ID不能为空")
    private Long seatId;

    @NotBlank(message = "预约日期不能为空")
    private String reserveDate;

    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;
}
