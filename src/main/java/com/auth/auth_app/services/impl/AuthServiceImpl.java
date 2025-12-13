package com.auth.auth_app.services.impl;

import com.auth.auth_app.dtos.UserDto;
import com.auth.auth_app.services.AuthService;
import com.auth.auth_app.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public UserDto registerUser(UserDto userDto) {
        UserDto user = userService.createUser(userDto);
        return user;
    }
}
