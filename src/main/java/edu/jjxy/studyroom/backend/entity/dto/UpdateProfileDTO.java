package edu.jjxy.studyroom.backend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新个人信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

    /**
     * 昵称
     */
    private String name;
}
