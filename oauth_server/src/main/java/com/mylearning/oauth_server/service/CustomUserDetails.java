package com.mylearning.oauth_server.service;

import com.mylearning.oauth_server.dto.User;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {

        /*String[] rolesArr = Objects.requireNonNull(user.getRoles()).split(",");

        return Arrays.stream(Objects.requireNonNull(rolesArr))
                .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());*/

        return Arrays.stream(
                        Optional.ofNullable(user.getRoles())
                                .orElse("")
                                .split(",")
                )
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    @Nonnull
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @Nonnull
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
