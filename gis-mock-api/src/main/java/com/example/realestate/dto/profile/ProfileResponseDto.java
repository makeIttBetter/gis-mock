package com.example.realestate.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Returned after reading/updating profile, 
 * containing the final (possibly updated) username.
 */
@Data
@AllArgsConstructor
public class ProfileResponseDto {
    private String username;
}
