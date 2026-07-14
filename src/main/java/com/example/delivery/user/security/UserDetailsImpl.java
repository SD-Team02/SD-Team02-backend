package com.example.delivery.user.security;

import com.example.delivery.global.config.JpaAuditingConfig;
import com.example.delivery.user.entity.Role;
import com.example.delivery.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.delivery.global.config.JpaAuditingConfig.CustomUserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails, JpaAuditingConfig.CustomUserDetails {

    private final User user;
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    @Override
    public Long getUserId() {
        return user.getUserId();
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public Long getUserId() { return user.getUserId(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role.name());
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }



}
