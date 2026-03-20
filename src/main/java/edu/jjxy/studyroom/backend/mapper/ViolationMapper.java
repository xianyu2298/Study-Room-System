package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Violation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 违规记录 Mapper
 */
@Mapper
public interface ViolationMapper extends BaseMapper<Violation> {

    @Select("SELECT * FROM violation WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Violation> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM violation WHERE user_id = #{userId} AND type = #{type} AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    int countByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
}
