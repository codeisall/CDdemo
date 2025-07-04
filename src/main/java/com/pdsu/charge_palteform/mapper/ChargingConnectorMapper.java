package com.pdsu.charge_palteform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdsu.charge_palteform.entity.ChargingConnector;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChargingConnectorMapper  extends BaseMapper<ChargingConnector> {
    /**
     * 统计充电站下各状态充电桩数量
     */
    @Select("SELECT status, COUNT(*) as count FROM charging_connectors " +
            "WHERE station_id = #{stationId} GROUP BY status")
    List<Map<String, Object>> countConnectorsByStatus(@Param("stationId") String stationId);

    /**
     * 批量统计多个充电站的充电桩状态
     */
    @Select("<script>" +
            "SELECT station_id, status, COUNT(*) as count FROM charging_connectors " +
            "WHERE station_id IN " +
            "<foreach collection='stationIds' item='stationId' open='(' separator=',' close=')'>" +
            "#{stationId}" +
            "</foreach>" +
            " GROUP BY station_id, status" +
            "</script>")
    List<Map<String, Object>> batchCountConnectorsByStatus(@Param("stationIds") List<String> stationIds);

}
