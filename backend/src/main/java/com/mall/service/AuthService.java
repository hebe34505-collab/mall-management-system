package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.config.AppProperties;
import com.mall.domain.AccessPolicy;
import com.mall.dto.LoginRequest;
import com.mall.dto.PasswordChangeRequest;
import com.mall.dto.ProfileUpdateRequest;
import com.mall.dto.RegisterRequest;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import com.mall.security.JwtService;
import com.mall.security.LoginUser;
import com.mall.security.SecurityUtils;
import com.mall.vo.LoginVO;
import com.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final OperationLogService operationLogService;

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION, "两次输入的密码不一致");
        }
        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (usernameCount != null && usernameCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setRole(AccessPolicy.retainRole("USER"));
        user.setStatus(1);
        user.setTokenVersion(0);
        userMapper.insert(user);
        operationLogService.record(user.getId(), user.getUsername(), "AUTH", "REGISTER", "用户注册");
    }

    public LoginVO login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount().trim(), request.getPassword()));
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper.eq(User::getUsername, request.getAccount().trim())
                        .or().eq(User::getEmail, request.getAccount().trim()))
                .last("LIMIT 1"));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }
        LoginUser loginUser = LoginUser.from(user);
        String token = jwtService.createToken(loginUser);
        operationLogService.record(user.getId(), user.getUsername(), "AUTH", "LOGIN", "登录成功");
        return new LoginVO(token, "Bearer", appProperties.getJwt().getExpirationMinutes(), UserVO.from(user));
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout() {
        LoginUser loginUser = SecurityUtils.current();
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        user.setTokenVersion(user.getTokenVersion() + 1);
        userMapper.updateById(user);
        operationLogService.record(user.getId(), user.getUsername(), "AUTH", "LOGOUT", "退出登录");
    }

    public UserVO me() {
        return UserVO.from(requireCurrentUser());
    }

    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(ProfileUpdateRequest request) {
        User user = requireCurrentUser();
        user.setNickname(request.getNickname().trim());
        user.setPhone(blankToNull(request.getPhone()));
        user.setAvatar(blankToNull(request.getAvatar()));
        user.setRole(AccessPolicy.retainRole(user.getRole()));
        userMapper.updateById(user);
        operationLogService.record(user.getId(), user.getUsername(), "USER", "PROFILE", "修改个人资料");
        return UserVO.from(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(PasswordChangeRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION, "两次输入的新密码不一致");
        }
        User user = requireCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setRole(AccessPolicy.retainRole(user.getRole()));
        userMapper.updateById(user);
        operationLogService.record(user.getId(), user.getUsername(), "USER", "PASSWORD", "修改密码");
    }

    private User requireCurrentUser() {
        User user = userMapper.selectById(SecurityUtils.current().getId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        return user;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
