package com.pdsu.charge_palteform.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdsu.charge_palteform.entity.User;
import com.pdsu.charge_palteform.entity.dto.LoginRequest;
import com.pdsu.charge_palteform.entity.dto.LoginResponse;
import com.pdsu.charge_palteform.entity.dto.UpdateUserRequest;
import com.pdsu.charge_palteform.exception.BusinessException;
import com.pdsu.charge_palteform.mapper.UserMapper;
import com.pdsu.charge_palteform.service.UserService;
import com.pdsu.charge_palteform.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final WxMaService wxMaService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 调用微信接口获取用户信息
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService()
                    .getSessionInfo(request.getCode());

            String openid = sessionInfo.getOpenid();
            String unionid = sessionInfo.getUnionid();

            if (openid == null) {
                throw new BusinessException("微信登录失败，请重试");
            }

            // 2. 查询用户是否存在
            User user = getUserByOpenid(openid);
            boolean isNewUser = false;

            if (user == null) {
                // 3. 新用户注册
                user = new User();
                user.setOpenid(openid);
                user.setUnionid(unionid);
                user.setNickname(request.getNickname());
                user.setAvatarUrl(request.getAvatarUrl());
                user.setBalance(BigDecimal.ZERO);
                user.setStatus(1);

                save(user);
                isNewUser = true;
                log.info("新用户注册成功, openid: {}", openid);
            } else {
                // 4. 老用户更新信息
                if (request.getNickname() != null) {
                    user.setNickname(request.getNickname());
                }
                if (request.getAvatarUrl() != null) {
                    user.setAvatarUrl(request.getAvatarUrl());
                }
                updateById(user);
                log.info("用户信息更新成功, userId: {}", user.getId());
            }

            // 5. 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), openid);

            // 6. 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setNickname(user.getNickname());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setPhone(user.getPhone());
            response.setBalance(user.getBalance());
            response.setIsNewUser(isNewUser);

            log.info("登录响应: {}", response);
            return response;

        } catch (Exception e) {
            log.error("用户登录失败", e);
            throw new BusinessException("登录失败，请重试");
        }
    }

    @Override
    public User getUserById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        log.info("获取用户userid:{},内容为:{}", userId,user);
        return user;
    }

    @Override
    public User getUserByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid));
    }

    @Override
    @Transactional
    public String  bindPhone(Long userId, String code) {
        try {
            // 使用微信服务解密获取手机号
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(code);
            String phone = phoneInfo.getPhoneNumber();

            if (phone == null || phone.isEmpty()) {
                throw new BusinessException("获取手机号失败");
            }

            // 更新用户手机号
            User user = getById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            user.setPhone(phone);
            updateById(user);
            log.info("用户手机号绑定成功, userId: {}, phone: {}", userId, phone);
            return phone;
        } catch (Exception e) {
            log.error("绑定手机号失败", e);
            throw new BusinessException("绑定手机号失败");
        }
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 更新允许修改的字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        updateById(user);
        log.info("用户信息更新成功, userId: {}", userId);
    }
}
