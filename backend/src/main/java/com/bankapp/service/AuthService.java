package com.bankapp.service;

import com.bankapp.dto.request.LoginRequest;
import com.bankapp.dto.request.RegisterRequest;
import com.bankapp.dto.response.JwtResponse;
import com.bankapp.dto.response.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest request);
    JwtResponse login(LoginRequest request);
}
