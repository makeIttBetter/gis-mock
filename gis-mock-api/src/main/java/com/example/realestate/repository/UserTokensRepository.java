// File: src/main/java/com/example/realestate/repository/UserTokensRepository.java
package com.example.realestate.repository;

import com.example.realestate.model.UserTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokensRepository extends JpaRepository<UserTokens, String> {
    // findById, save, etc. come out of the box
}
