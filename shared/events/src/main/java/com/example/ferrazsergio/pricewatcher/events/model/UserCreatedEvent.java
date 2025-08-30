package com.example.ferrazsergio.pricewatcher.events.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event fired when a user is created
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {
    
    private Long userId;
    private String username;
    private String email;
    
    public UserCreatedEvent(Long userId, String username, String email) {
        super("USER_CREATED");
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}