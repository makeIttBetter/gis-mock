package com.example.realestate.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * For user sign-up or OAuth2 upgrades.
 * Adjust fields as you want. You might want:
 * - username
 * - email
 * - password
 * - provider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    private String username;
    private String password;
    private String provider;
}
