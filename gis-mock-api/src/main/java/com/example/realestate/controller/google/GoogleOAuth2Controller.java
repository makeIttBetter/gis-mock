package com.example.realestate.controller.google;

import com.example.realestate.service.google.GoogleOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/google/oauth2")
@RequiredArgsConstructor
public class GoogleOAuth2Controller {

    private final GoogleOAuth2Service oauth2;

    /* ---------- STEP 1 – start flow ---------- */
    @GetMapping("/authorize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> startAuthorization() {
        String url = oauth2.buildAuthorizationUrl();
        System.out.println("Google OAuth2 URL: " + url);
        return ResponseEntity.status(302).header("Location", url).build();
    }

    /* ---------- STEP 2 – Google callback ---------- */
    @GetMapping("/callback")
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code) {
        oauth2.exchangeCodeForTokensAndSave(code);
        // a tiny HTML is fine; here we just tell the user to close the popup.
        return ResponseEntity.ok(
                "<html><body><p>Google login successful. You can close this window.</p>"
                        + "<script>window.close();</script></body></html>");
    }

    /* ---------- STEP 3 – frontend can verify ---------- */
    @GetMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> checkToken() {
        try {
            boolean valid = oauth2.verifyCurrentUserToken();
            return Map.of("valid", valid);
        } catch (Exception e) {
            log.error("Error verifying token: {}", e.getMessage());
            return Map.of("valid", false);
        }
    }
}
