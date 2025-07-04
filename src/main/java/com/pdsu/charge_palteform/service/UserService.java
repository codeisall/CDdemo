package com.pdsu.charge_palteform.service;


import com.pdsu.charge_palteform.entity.User;
import com.pdsu.charge_palteform.entity.dto.LoginRequest;
import com.pdsu.charge_palteform.entity.dto.LoginResponse;
import com.pdsu.charge_palteform.entity.dto.UpdateUserRequest;

public interface UserService {
    LoginResponse login(LoginRequest request);
    User getUserById(Long userId);
    User getUserByOpenid(String openid);
    String  bindPhone(Long userId, String code);
    void updateUserInfo(Long userId, UpdateUserRequest request);
}
