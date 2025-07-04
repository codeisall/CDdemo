package com.pdsu.charge_palteform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdsu.charge_palteform.entity.ChargingStation;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ChargingStationMapper extends BaseMapper<ChargingStation> {


    List<ChargingStation> findNearbyStations(@Param("latitude") BigDecimal latitude,
                                             @Param("longitude") BigDecimal longitude,
                                             @Param("radius") Integer radius,
                                             @Param("offset") Integer offset,
                                             @Param("size") Integer size);

    Long countNearbyStations(@Param("latitude") BigDecimal latitude,
                             @Param("longitude") BigDecimal longitude,
                             @Param("radius") Integer radius);
}
