package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
