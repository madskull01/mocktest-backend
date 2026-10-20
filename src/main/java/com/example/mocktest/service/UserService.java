package com.example.mocktest.service;

import com.example.mocktest.dto.auth.LoginRequest;
import com.example.mocktest.dto.auth.LoginResponse;
import com.example.mocktest.dto.user.CreateUserRequest;
import com.example.mocktest.dto.user.UpdateUserRequest;
import com.example.mocktest.dto.user.UserResponse;
import com.example.mocktest.model.User;

import java.util.List;

public interface UserService {

    UserResponse createUser(
            CreateUserRequest request
    );

    LoginResponse authenticate(LoginRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    );

    void deleteUser(Long id);
}
