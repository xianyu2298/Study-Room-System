package edu.jjxy.studyroom.backend.entity.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * 公告创建/更新 DTO
 */
@Data
public class NoticeDTO {

    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @Length(max = 50, message = "标题长度不能超过50")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private Integer isTop;

    @NotBlank(message = "生效时间不能为空")
    private String startTime;

    @NotBlank(message = "失效时间不能为空")
    private String endTime;
}
