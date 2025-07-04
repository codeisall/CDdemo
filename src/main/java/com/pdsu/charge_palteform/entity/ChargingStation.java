package com.pdsu.charge_palteform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_stations")
public class ChargingStation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String stationId;

    private String stationName;

    private String address;

    private String province;

    private String city;

    private String district;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String stationTel;

    private BigDecimal serviceFee;

    private String parkingFee;

    private String openingHours;

    private Integer stationStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
