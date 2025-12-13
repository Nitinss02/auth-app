package com.auth.auth_app.services;

import com.auth.auth_app.dtos.UserDto;

public interface AuthService {
    public UserDto registerUser(UserDto userDto);
}
