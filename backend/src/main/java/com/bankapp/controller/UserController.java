package com.bankapp.controller;

import com.bankapp.dto.request.UpdateProfileRequest;
import com.bankapp.dto.response.ApiResponse;
import com.bankapp.dto.response.UserDto;
import com.bankapp.security.UserPrincipal;
import com.bankapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(userService.getProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", userService.updateProfile(principal.getId(), request));
    }
}
