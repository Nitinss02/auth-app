package com.auth.auth_app.services.impl;

import com.auth.auth_app.dtos.UserDto;
import com.auth.auth_app.exceptions.EmailAlreadyExit;
import com.auth.auth_app.exceptions.ResourceNotFoundException;
import com.auth.auth_app.exceptions.UserNotFoundWithNameException;
import com.auth.auth_app.models.User;
import com.auth.auth_app.repository.UserRepository;
import com.auth.auth_app.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDto createUser(UserDto userDtos) {
        if(userDtos.getEmail()==null || userDtos.getEmail().isEmpty())
        {
            throw new IllegalArgumentException("Email is not found");
        }
        if(userRepository.existsByEmail(userDtos.getEmail()))
        {
            throw  new EmailAlreadyExit();
        }
        User user = modelMapper.map(userDtos , User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public List<UserDto> getAllUser() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();

    }

    @Override
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User is not found in give Id"));
        return modelMapper.map(user,UserDto.class);

    }

    @Override
    public UserDto getUserByEmail(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not Found with this email : "+email));
        UserDto users = modelMapper.map(user, UserDto.class);
        log.info("User data is : {}",user.getEmail());
        return users;
    }

    @Override
    public UserDto getUserByName(String name) {
        User user = userRepository.findByName(name).orElseThrow(() -> new UserNotFoundWithNameException("User not found with name"));
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional
    @Override
    public String deleteUser(UUID userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User is not found with given id"));
        if(user.getId() == userId)
        {
            userRepository.delete(user);
            return "User deleted Successfully";
        }else {
            return "something want wrong";
        }

    }

    @Override
    public UserDto updateUser(UUID userId, UserDto userDto) {
        User exitingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with given Id"));
        if (userDto.getName()!=null) exitingUser.setName(userDto.getName());
        if(userDto.getPassword()!=null) exitingUser.setPassword(userDto.getPassword());
        if(userDto.getImage()!=null) exitingUser.setImage(userDto.getImage());
        exitingUser.setEnable(userDto.isEnable());
        if (userDto.getProvider()!=null) exitingUser.setProvider(userDto.getProvider());
        if (userDto.getRoles() != null) exitingUser.setRoles(userDto.getRoles());

        User saved = userRepository.save(exitingUser);
        return modelMapper.map(saved,UserDto.class);
    }
}
