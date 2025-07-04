package com.pdsu.charge_palteform.entity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationQueryRequest {
    @DecimalMin(value = "-90.0", message = "纬度范围错误")
    @DecimalMax(value = "90.0", message = "纬度范围错误")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "经度范围错误")
    @DecimalMax(value = "180.0", message = "经度范围错误")
    private BigDecimal longitude;

    @Min(value = 1, message = "搜索半径最小1公里")
    @Max(value = 50, message = "搜索半径最大50公里")
    private Integer radius = 10; // 默认10公里

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页面大小最小为1")
    @Max(value = 50, message = "页面大小最大为50")
    private Integer pageSize = 10;

    private String keyword; // 搜索关键词

    private Integer connectorType; // 充电桩类型筛选：1-直流，2-交流

    private Integer status; // 充电站状态筛选
}
