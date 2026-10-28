package com.mall.security;

import com.mall.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final Integer status;
    private final Integer tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public LoginUser(Long id, String username, String password, String role, Integer status, Integer tokenVersion) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        this.tokenVersion = tokenVersion;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public static LoginUser from(User user) {
        return new LoginUser(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.getRole(), user.getStatus(), user.getTokenVersion());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
