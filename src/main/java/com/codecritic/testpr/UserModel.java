package com.codecritic.testpr;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple model class representing a user in the system.
 * This class is not integrated with Spring and won't be picked up by component scanning.
 */
public class UserModel {
    
    private UUID id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean active;
    private List<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Constructor with mandatory fields
    public UserModel(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = true;
        this.permissions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
    
    // Default constructor
    public UserModel() {
        this.id = UUID.randomUUID();
        this.active = false;
        this.permissions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }
    
    // Add a permission to the user
    public void addPermission(String permission) {
        if (permission != null && !permission.isEmpty()) {
            if (this.permissions == null) {
                this.permissions = new ArrayList<>();
            }
            this.permissions.add(permission);
        }
    }
    
    // Check if user has a specific permission
    public boolean hasPermission(String permission) {
        return this.permissions != null && this.permissions.contains(permission);
    }
    
    // Record a login event
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // Create a secure copy of the user without sensitive information
    public UserModel createSecureCopy() {
        UserModel secureCopy = new UserModel();
        secureCopy.id = this.id;
        secureCopy.username = this.username;
        secureCopy.email = this.email;
        // Don't copy password
        secureCopy.firstName = this.firstName;
        secureCopy.lastName = this.lastName;
        secureCopy.active = this.active;
        // Make a defensive copy of permissions
        if (this.permissions != null) {
            secureCopy.permissions = new ArrayList<>(this.permissions);
        }
        secureCopy.createdAt = this.createdAt;
        secureCopy.lastLogin = this.lastLogin;
        return secureCopy;
    }
    
    // Create a string representation with a potential security issue
    public String toString() {
        return "User{id=" + id + 
               ", username='" + username + '\'' + 
               ", email='" + email + '\'' + 
               ", password='" + password + '\'' +  // Security issue: exposing password in toString
               ", firstName='" + firstName + '\'' + 
               ", lastName='" + lastName + '\'' + 
               ", active=" + active + 
               ", permissions=" + permissions + 
               ", createdAt=" + createdAt + 
               ", lastLogin=" + lastLogin + 
               '}';
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;  // Security issue: direct password getter
    }

    public void setPassword(String password) {
        this.password = password;  // Security issue: no encryption
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getPermissions() {
        return permissions;  // Security issue: returns reference to mutable list
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;  // Security issue: uses reference not copy
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}