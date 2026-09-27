package com.bankapp.service.impl;

import com.bankapp.dto.request.UpdateProfileRequest;
import com.bankapp.dto.response.PageResponse;
import com.bankapp.dto.response.UserDto;
import com.bankapp.entity.User;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.mapper.UserMapper;
import com.bankapp.repository.UserRepository;
import com.bankapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto getProfile(Long userId) {
        return UserMapper.toDto(getUserEntityOrThrow(userId));
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserEntityOrThrow(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        User saved = userRepository.save(user);
        log.info("Profile updated for user id: {}", userId);
        return UserMapper.toDto(saved);
    }

    @Override
    public User getUserEntityOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public PageResponse<UserDto> searchCustomers(String keyword, int page, int size) {
        Page<User> result;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (keyword == null || keyword.isBlank()) {
            result = userRepository.findAll(pageRequest);
        } else {
            result = userRepository
                    .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                            keyword, keyword, keyword, keyword, pageRequest);
        }
        return PageResponse.from(result.map(UserMapper::toDto));
    }

    @Override
    @Transactional
    public UserDto setEnabled(Long userId, boolean enabled) {
        User user = getUserEntityOrThrow(userId);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        log.info("User {} {} by admin", userId, enabled ? "enabled" : "disabled");
        return UserMapper.toDto(saved);
    }
}
