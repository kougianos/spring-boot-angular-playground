package com.springboot.starter.service;

import com.springboot.starter.model.security.JwtResponse;
import com.springboot.starter.model.security.LoginRequest;
import com.springboot.starter.model.security.SignupRequest;
import com.springboot.starter.model.security.UserInfoResponse;
import com.springboot.starter.model.security.Role;
import com.springboot.starter.model.persistence.User;
import com.springboot.starter.repository.UserRepository;
import com.springboot.starter.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public void registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.username())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.email())) {
            throw new IllegalArgumentException("Email is already in use!");
        }        User user = User.builder()
                .username(signupRequest.username())
                .email(signupRequest.email())
                .password(passwordEncoder.encode(signupRequest.password()))
                .roles(Collections.singletonList(Role.ROLE_USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.usernameOrEmail(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateJwtToken(authentication);
        
        return new JwtResponse(jwt);
    }

    public UserInfoResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return new UserInfoResponse(user.getUsername(), user.getEmail());
    }
}
