package edu.jjxy.studyroom.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.jjxy.studyroom.backend.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalTime;
import java.util.List;

/**
 * 自习室Mapper
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 根据自习室编号查询
     */
    @Select("SELECT * FROM room WHERE room_no = #{roomNo} LIMIT 1")
    Room selectByRoomNo(@Param("roomNo") String roomNo);

    /**
     * 根据自习室名称查询
     */
    @Select("SELECT * FROM room WHERE name = #{name} LIMIT 1")
    Room selectByName(@Param("name") String name);

    /**
     * 查询已启用的自习室列表
     */
    @Select("SELECT * FROM room WHERE status = 0 ORDER BY area, room_no")
    List<Room> selectActiveRooms();

    /**
     * 查询指定区域的自习室
     */
    @Select("SELECT * FROM room WHERE area = #{area} AND status = 0 ORDER BY room_no")
    List<Room> selectByArea(@Param("area") String area);

    /**
     * 查询所有区域（去重）
     */
    @Select("SELECT DISTINCT area FROM room WHERE status = 0 ORDER BY area")
    List<String> selectAllAreas();

    /**
     * 统计自习室总座位数
     */
    @Select("SELECT COUNT(*) FROM seat WHERE room_id = #{roomId}")
    int countSeatsByRoomId(@Param("roomId") Long roomId);

    /**
     * 统计自习室有效预约数
     */
    @Select("SELECT COUNT(*) FROM reserve WHERE room_id = #{roomId} AND status IN (0, 1)")
    int countEffectiveReservesByRoomId(@Param("roomId") Long roomId);

    /**
     * 批量禁用自习室下的座位
     */
    @Update("UPDATE seat SET status = 3 WHERE room_id = #{roomId}")
    int disableSeatsByRoomId(@Param("roomId") Long roomId);
}
