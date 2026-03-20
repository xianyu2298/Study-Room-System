package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Reserve;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约 Mapper
 */
@Mapper
public interface ReserveMapper extends BaseMapper<Reserve> {

    @Select("SELECT * FROM reserve WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Reserve> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM reserve WHERE user_id = #{userId} AND status IN (0, 1) LIMIT 1")
    Reserve selectActiveByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM reserve WHERE seat_id = #{seatId} AND status IN (0, 1) LIMIT 1")
    Reserve selectActiveBySeatId(@Param("seatId") Long seatId);

    @Select("SELECT COUNT(*) FROM reserve WHERE user_id = #{userId} AND status = 4 AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    int countNoShowByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM reserve WHERE room_id = #{roomId} AND status = #{status}")
    int countByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM reserve WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM reserve WHERE DATE(create_time) = CURDATE() AND status IN (0, 1, 2)")
    int countTodayReserves();

    @Update("UPDATE reserve SET status = #{status} WHERE id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM reserve WHERE user_id = #{userId} AND status IN (0, 1, 2)")
    int countTotalByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM reserve WHERE user_id = #{userId} AND status = 2")
    int countCompletedByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM reserve WHERE user_id = #{userId} AND status IN (0, 1) AND DATE(start_time) = CURDATE()")
    int countPendingTodayByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM reserve WHERE user_id = #{userId} AND status IN (0, 1)")
    int countActiveByUserId(@Param("userId") Long userId);
}
