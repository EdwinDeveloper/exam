package com.monarch.pokedex.adapter.in.web;

import com.monarch.pokedex.config.UserEntity;
import com.monarch.pokedex.config.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    public AuthController(UserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        this.users = users;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody Registration request) {
        String username = request.username().trim();
        if (users.existsByUsername(username)) throw new IllegalArgumentException("Username is already registered");
        users.save(new UserEntity(username, encoder.encode(request.password())));
        return Map.of("username", username, "message", "Account created. Sign in to receive an access token.");
        }

        @PostMapping("/login")
        public TokenResponse login(@Valid @RequestBody Login request) {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(1, ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("pokedex-api")
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(authentication.getName())
            .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", 3600, authentication.getName());
    }

    public record Registration(@NotBlank @Size(min = 3, max = 40) String username,
                               @NotBlank @Size(min = 8, max = 72) String password) {}
        public record Login(@NotBlank @Size(min = 3, max = 40) String username,
                @NotBlank @Size(max = 72) String password) {}
        public record TokenResponse(String accessToken, String tokenType, long expiresIn, String username) {}
}
