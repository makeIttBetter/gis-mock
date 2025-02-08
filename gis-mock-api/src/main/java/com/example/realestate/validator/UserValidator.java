package com.example.realestate.validator;

import com.example.realestate.dto.UserAuthDTO;
import com.example.realestate.exceptions.UserAlreadyExistsException;
import com.example.realestate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateUserDoesNotExist(UserAuthDTO userAuthDTO) {
        // Check email or username if already exists
        if (userRepository.existsByUsername(userAuthDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + userAuthDTO.getUsername());
        }
        if (userRepository.existsByEmail(userAuthDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + userAuthDTO.getEmail());
        }
    }
}
