package com.campus.issue_tracker.controller;

import com.campus.issue_tracker.dto.LoginRequest;
import com.campus.issue_tracker.dto.SignupRequest;
import com.campus.issue_tracker.entity.Role;
import com.campus.issue_tracker.entity.User;
import com.campus.issue_tracker.repository.UserRepository;
import com.campus.issue_tracker.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    // ================= REGISTER =================
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(
                signUpRequest.getRole() != null
                        ? signUpRequest.getRole()
                        : Role.ROLE_STUDENT
        );

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }

    // ================= LOGIN (FIXED) =================
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),   // ✅ FIXED
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateTokenFromUsername(authentication.getName());

        Cookie cookie = new Cookie("campus_jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // production වල true
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok("Login successful! Cookie has been set.");
    }

    // ================= LOGOUT =================
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {

        Cookie cookie = new Cookie("campus_jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out successfully!");
    }
}
