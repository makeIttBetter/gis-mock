package com.example.realestate.service;

import com.example.realestate.dto.profile.ProfileRequestDto;
import com.example.realestate.dto.profile.ProfileResponseDto;
import com.example.realestate.model.User;
import com.example.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles logic for reading/updating a user's profile (username/password).
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponseDto getCurrentUserProfile(Authentication authentication) {
        // Typically, authentication.getName() might be the username or ID 
        // depending on how you set up security. 
        // Let's assume it's the user ID or we have a method to get that.
        String currentUsername = authentication.getName();

        // Find the user by username or ID, whichever your system uses:
        User user = userRepository.findByUsername(currentUsername);
        // or if you're storing user ID in principal, do userRepository.findById(...)

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return new ProfileResponseDto(user.getUsername());
        // Add more fields if needed, e.g. user.getEmail()
    }

    /**
     * Update username and/or password.
     * If newPassword is given, we must verify oldPassword first.
     */
    public ProfileResponseDto updateCurrentUserProfile(
            Authentication authentication,
            ProfileRequestDto dto
    ) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Update username if provided
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            user.setUsername(dto.getUsername().trim());
        }

        // If user wants to change password, check old & new
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            // Verify old password
            if (dto.getOldPassword() == null || dto.getOldPassword().isBlank()) {
                throw new RuntimeException("Old password is required to set a new password.");
            }
            boolean matches = passwordEncoder.matches(dto.getOldPassword(), user.getPassword());
            if (!matches) {
                throw new RuntimeException("Old password is incorrect.");
            }

            // Check confirm password if you like
            if (dto.getConfirmNewPassword() != null &&
                    !dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
                throw new RuntimeException("New password and confirm password do not match.");
            }

            // Check that new password is not empty
            if (dto.getNewPassword().isBlank()) {
                throw new RuntimeException("New password cannot be empty.");
            }

            // Encode new password
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepository.save(user);

        return new ProfileResponseDto(user.getUsername());
    }
}
