package com.example.ferrazsergio.pricewatcher.userservice.controller;

import com.example.ferrazsergio.pricewatcher.common.dto.ApiResponse;
import com.example.ferrazsergio.pricewatcher.common.dto.PagedResponse;
import com.example.ferrazsergio.pricewatcher.userservice.dto.LoginRequest;
import com.example.ferrazsergio.pricewatcher.userservice.dto.UserRegistrationRequest;
import com.example.ferrazsergio.pricewatcher.userservice.dto.UserResponse;
import com.example.ferrazsergio.pricewatcher.userservice.service.UserService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Timed(value = "user.register", description = "Time taken to register a user")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Registering user with username: {}", request.username());
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User registered successfully"));
    }

    @PostMapping("/login")
    @Timed(value = "user.login", description = "Time taken to authenticate a user")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.username());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        
        // For now, return a simple success message
        // In a real implementation, you would generate JWT tokens here
        String message = "Login successful for user: " + authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/{id}")
    @Timed(value = "user.get", description = "Time taken to get a user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    @Timed(value = "user.getByUsername", description = "Time taken to get a user by username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @Timed(value = "user.getAll", description = "Time taken to get all users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        
        PagedResponse<UserResponse> pagedResponse = PagedResponse.of(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @PutMapping("/{id}")
    @Timed(value = "user.update", description = "Time taken to update a user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Timed(value = "user.delete", description = "Time taken to delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}