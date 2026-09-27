package com.bankapp.mapper;

import com.bankapp.dto.response.UserDto;
import com.bankapp.entity.Role;
import com.bankapp.entity.User;

import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
