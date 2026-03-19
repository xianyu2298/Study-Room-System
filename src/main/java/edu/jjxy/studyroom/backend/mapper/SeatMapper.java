package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 座位Mapper
 */
@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    /**
     * 根据自习室ID查询所有有效座位（排除禁用/维护中）
     */
    @Select("SELECT * FROM seat WHERE room_id = #{roomId} AND status NOT IN (3, 4) ORDER BY seat_no")
    List<Seat> selectActiveSeatsByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据自习室ID查询所有座位
     */
    @Select("SELECT * FROM seat WHERE room_id = #{roomId} ORDER BY seat_no")
    List<Seat> selectAllSeatsByRoomId(@Param("roomId") Long roomId);

    /**
     * 根据自习室ID和座位号查询
     */
    @Select("SELECT * FROM seat WHERE room_id = #{roomId} AND seat_no = #{seatNo} LIMIT 1")
    Seat selectByRoomIdAndSeatNo(@Param("roomId") Long roomId, @Param("seatNo") String seatNo);

    /**
     * 统计自习室已添加的座位数
     */
    @Select("SELECT COUNT(*) FROM seat WHERE room_id = #{roomId}")
    int countByRoomId(@Param("roomId") Long roomId);

    /**
     * 批量更新座位状态
     */
    @Update("<script>" +
            "UPDATE seat SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 查询热门座位（按7天预约量排序）
     */
    @Select("SELECT s.* FROM seat s " +
            "INNER JOIN reserve r ON s.id = r.seat_id " +
            "WHERE s.room_id = #{roomId} AND s.status = 0 " +
            "AND r.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(r.id) DESC " +
            "LIMIT 10")
    List<Seat> selectHotSeatsByRoomId(@Param("roomId") Long roomId);

    /**
     * 重置自习室所有座位状态为可预约
     */
    @Update("UPDATE seat SET status = 0 WHERE room_id = #{roomId} AND status IN (1, 2)")
    int resetSeatsByRoomId(@Param("roomId") Long roomId);
}
