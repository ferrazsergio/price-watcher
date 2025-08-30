package com.example.ferrazsergio.pricewatcher.userservice.service;

import com.example.ferrazsergio.pricewatcher.common.exception.BusinessException;
import com.example.ferrazsergio.pricewatcher.common.exception.ResourceNotFoundException;
import com.example.ferrazsergio.pricewatcher.events.model.UserCreatedEvent;
import com.example.ferrazsergio.pricewatcher.userservice.dto.UserRegistrationRequest;
import com.example.ferrazsergio.pricewatcher.userservice.dto.UserResponse;
import com.example.ferrazsergio.pricewatcher.userservice.model.User;
import com.example.ferrazsergio.pricewatcher.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ferrazsergio.pricewatcher.events.config.RabbitMQConfig.*;

/**
 * Service for user management and authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public UserResponse createUser(UserRegistrationRequest request) {
        log.info("Creating user with username: {}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        user = userRepository.save(user);

        // Publish user created event
        UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getUsername(), user.getEmail());
        rabbitTemplate.convertAndSend(PRICE_WATCHER_EXCHANGE, USER_CREATED_ROUTING_KEY, event);

        log.info("User created successfully with ID: {}", user.getId());
        return mapToResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return mapToResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRegistrationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Check if username is taken by another user
        if (!user.getUsername().equals(request.username()) && 
            userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists");
        }

        // Check if email is taken by another user
        if (!user.getEmail().equals(request.email()) && 
            userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());

        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        user = userRepository.save(user);
        log.info("User updated successfully with ID: {}", user.getId());
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", id);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}