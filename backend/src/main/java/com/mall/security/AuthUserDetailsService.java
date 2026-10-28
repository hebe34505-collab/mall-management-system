package com.mall.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 登录账号可以是用户名或邮箱。不存在时抛出 UsernameNotFoundException，
 * Spring Security 会把它转换成“用户名或密码错误”，避免暴露账号是否存在。
 */
@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String account) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .and(wrapper -> wrapper.eq(User::getUsername, account).or().eq(User::getEmail, account))
                .last("LIMIT 1"));
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在");
        }
        return LoginUser.from(user);
    }
}
