package com.mylearning.oauth_server.service;

import com.mylearning.oauth_server.dto.User;
import com.mylearning.oauth_server.repo.UserRepo;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Nonnull
    @Override
    public UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {

        User user = userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();*/

        return new CustomUserDetails(user);

    }
}
