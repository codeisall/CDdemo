package com.pdsu.charge_palteform.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private BigDecimal balance;
    private Boolean isNewUser;
}
