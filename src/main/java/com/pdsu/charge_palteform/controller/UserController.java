package com.pdsu.charge_palteform.controller;

import com.pdsu.charge_palteform.entity.User;
import com.pdsu.charge_palteform.entity.dto.BindPhoneRequest;
import com.pdsu.charge_palteform.entity.dto.UpdateUserRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import com.pdsu.charge_palteform.service.UserService;
import com.pdsu.charge_palteform.common.Result;

@Tag(name = "用户信息", description = "用户个人信息相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind_phone")
    public Result<?> bindPhone(HttpServletRequest request, @RequestBody BindPhoneRequest bindRequest) {
        Long userId = (Long) request.getAttribute("userId");
        String phone = userService.bindPhone(userId, bindRequest.getCode());
        return Result.success(phone);
    }

    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public Result<?> updateUserInfo(HttpServletRequest request,@RequestBody UpdateUserRequest updateRequest
    ) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, updateRequest);
        return Result.success();
    }

}
