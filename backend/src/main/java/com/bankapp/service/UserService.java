package com.bankapp.service;

import com.bankapp.dto.request.UpdateProfileRequest;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.UserDto;
import com.bankapp.entity.User;

public interface UserService {
    UserDto getProfile(Long userId);
    UserDto updateProfile(Long userId, UpdateProfileRequest request);
    User getUserEntityOrThrow(Long userId);
    PageResponse<UserDto> searchCustomers(String keyword, int page, int size);
    UserDto setEnabled(Long userId, boolean enabled);
}
