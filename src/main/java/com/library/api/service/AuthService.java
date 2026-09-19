package com.library.api.service;

import com.library.api.dto.AuthResponse;
import com.library.api.dto.LoginRequest;
import com.library.api.dto.RegisterRequest;
import com.library.api.exception.UsernameTakenException;
import com.library.api.model.User;
import com.library.api.repository.UserRepository;
import com.library.api.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /** Public registration always creates a regular user, admins are created with {@link #createAdmin}. */
    public AuthResponse register(RegisterRequest request) {
        User user = createUser(request.getUsername(), request.getPassword(), ROLE_USER);
        return new AuthResponse(jwtUtil.generateToken(user), user.getUsername(), user.getRole());
    }

    public void createAdmin(String username, String password) {
        createUser(username, password, ROLE_ADMIN);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthResponse(jwtUtil.generateToken(user), user.getUsername(), user.getRole());
    }

    private User createUser(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameTakenException(username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return userRepository.save(user);
    }
}
