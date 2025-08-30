package com.example.ferrazsergio.pricewatcher.userservice.dto;

import com.example.ferrazsergio.pricewatcher.userservice.model.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO for user response
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    UserRole role,
    boolean enabled,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {}