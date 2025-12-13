package com.auth.auth_app.services;

import com.auth.auth_app.dtos.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    //create User
    UserDto createUser(UserDto userDtos);

    List<UserDto> getAllUser();

    UserDto getUserById(UUID userId);

    //get userByemail
    UserDto getUserByEmail(String email);

    //get user by its name
    UserDto getUserByName(String name);

    //delete user
    String deleteUser(UUID userId);

    //Update User
    UserDto updateUser(UUID userId, UserDto userDto);

}
