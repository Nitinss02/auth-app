package com.auth.auth_app.services;

import com.auth.auth_app.dtos.UserDto;

public interface UserService {
    //create User
    UserDto createUser(UserDto userDtos);

    //get userByemail
    UserDto getUserByEmail(String email);

}
