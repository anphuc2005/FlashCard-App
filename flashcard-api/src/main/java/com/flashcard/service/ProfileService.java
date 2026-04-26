package com.flashcard.service;

import com.flashcard.dto.request.UpdateProfileRequest;
import com.flashcard.dto.response.UserProfileResponse;
import com.flashcard.exception.ResourceNotFoundException;
import com.flashcard.model.User;
import com.flashcard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(String email) {
        User user = findByEmail(email);
        return toUserProfile(user);
    }

    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        user.setDisplayName(request.getDisplayName().trim());
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            user.setAvatarUrl(request.getImageUrl().trim());
        }
        User updatedUser = userRepository.save(user);
        return toUserProfile(updatedUser);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserProfileResponse toUserProfile(User user) {
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
