package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价 Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT * FROM review WHERE reserve_id = #{reserveId} LIMIT 1")
    Review selectByReserveId(@Param("reserveId") Long reserveId);

    @Select("SELECT * FROM review WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Review> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT AVG(score) FROM review WHERE room_id = #{roomId}")
    Double getAvgScoreByRoomId(@Param("roomId") Long roomId);

    @Select("SELECT COUNT(*) FROM review WHERE room_id = #{roomId}")
    int countByRoomId(@Param("roomId") Long roomId);
}
