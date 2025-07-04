package com.pdsu.charge_palteform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_connectors")
public class ChargingConnector {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String connectorId;

    private String stationId;

    private String connectorName;

    private Integer connectorType;

    private BigDecimal ratedPower;

    private BigDecimal currentPower;

    private BigDecimal electricityFee;

    private BigDecimal serviceFee;

    private Integer status;

    private LocalDateTime statusUpdateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
