package com.pdsu.charge_palteform.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;

    private String nickname;

    private String avatarUrl;
}
