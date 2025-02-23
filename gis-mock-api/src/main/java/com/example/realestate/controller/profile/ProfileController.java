package com.example.realestate.controller.profile;


import com.example.realestate.dto.profile.ProfileRequestDto;
import com.example.realestate.dto.profile.ProfileResponseDto;
import com.example.realestate.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get the current user's profile info (e.g. current username).
     * We assume the user is authenticated.
     */
    @GetMapping
    public ProfileResponseDto getProfile(Authentication authentication) {
        // The 'authentication' should have the user's principal/ID. 
        // We'll pass it to the service to load the user profile.
        return profileService.getCurrentUserProfile(authentication);
    }

    /**
     * Update the current user's username and/or password.
     * <p>
     * - If oldPassword, newPassword are provided, we verify oldPassword
     * and then update the password.
     * - If username is provided, we update the username.
     */
    @PutMapping
    public ProfileResponseDto updateProfile(
            @RequestBody ProfileRequestDto profileRequestDto,
            Authentication authentication
    ) {
        log.info("Updating profile for user: {}", authentication.getName());
        log.info("DTO: {}", profileRequestDto);
        return profileService.updateCurrentUserProfile(authentication, profileRequestDto);
    }
}
