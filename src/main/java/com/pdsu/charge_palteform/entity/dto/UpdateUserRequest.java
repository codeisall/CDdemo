package com.pdsu.charge_palteform.entity.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String avatarUrl;
}
