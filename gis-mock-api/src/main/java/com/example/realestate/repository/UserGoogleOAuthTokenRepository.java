package com.example.realestate.repository;

import com.example.realestate.model.UserGoogleOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGoogleOAuthTokenRepository extends JpaRepository<UserGoogleOAuthToken, String> {

    Optional<UserGoogleOAuthToken> findByUserId(String userId);

    boolean existsByUserId(String userId);

    void deleteByUserId(String userId);
}
