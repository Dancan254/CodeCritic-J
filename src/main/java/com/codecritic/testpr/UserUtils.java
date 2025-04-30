package com.codecritic.testpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for user-related operations.
 * This class is not integrated with Spring and won't be picked up by component scanning.
 */
public class UserUtils {
    
    // In-memory cache with no eviction policy (potential memory leak)
    private static Map<String, Object> userCache = new HashMap<>();
    
    /**
     * Validates an email address using a simplified regex.
     * Has multiple issues for the AI to identify.
     */
    public static boolean validateEmail(String email) {
        if (email == null)
            return false;
            
        // Overly simplistic email validation (issue for review)
        return email.matches(".*@.*\\..+");
    }
    
    /**
     * Checks if a password meets security requirements.
     * Security requirement implementation is flawed.
     */
    public static boolean isSecurePassword(String password) {
        // Check password is not null or empty
        if (password == null || password.isEmpty())
            return false;
            
        // Check length
        if (password.length() < 8)
            return false;
            
        // Check for at least one digit (flawed implementation)
        if (!password.matches(".*\\d.*"))
            return false;
            
        // Check for uppercase (flawed implementation that accepts any character)
        boolean hasUppercase = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
                break;
            }
        }
        
        return hasUppercase;
    }
    
    /**
     * Generates a username from first and last name.
     * Has potential issues with special characters.
     */
    public static String generateUsername(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return "user" + System.currentTimeMillis();
        }
        
        // Performance issue: inefficient string concatenation
        String username = "";
        username = username + firstName.toLowerCase().charAt(0);
        username = username + lastName.toLowerCase();
        
        // Doesn't handle special characters or spaces
        return username;
    }
    
    /**
     * Filter users by permission.
     * Contains performance issues.
     */
    public static <T> List<T> filterByPermission(List<T> users, String requiredPermission) {
        if (users == null || requiredPermission == null) {
            return new ArrayList<>();
        }
        
        // Inefficient filtering (N² operations)
        List<T> result = new ArrayList<>();
        for (T user : users) {
            // Skip null users (potential NPE if we didn't check)
            if (user == null) continue;
            
            boolean hasPermission = false;
            for (T u : users) {
                if (u.equals(user) && hasPermissionHelper(u, requiredPermission)) {
                    hasPermission = true;
                    break;
                }
            }
            
            if (hasPermission) {
                result.add(user);
            }
        }
        
        return result;
    }
    
    // This would be better implemented with generics properly
    private static <T> boolean hasPermissionHelper(T user, String permission) {
        // This is a stub - in reality we'd check the user's permissions
        // Return random result for demo purposes
        return System.currentTimeMillis() % 2 == 0;
    }
    
    /**
     * Stores a user in the cache.
     * Potential memory leak as cache grows unbounded.
     */
    public static void storeUserInCache(String userId, Object userObject) {
        userCache.put(userId, userObject);
    }
    
    /**
     * Retrieves a user from the cache.
     */
    public static Object getUserFromCache(String userId) {
        return userCache.get(userId);
    }
    
    /**
     * Inefficient method to convert a list of user IDs to a pipe-separated string.
     */
    public static String convertUserIdsToString(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return "";
        }
        
        // Inefficient implementation for demo purposes
        StringBuilder result = new StringBuilder();
        for (String userId : userIds) {
            if (!result.isEmpty()) {
                result.append("|");
            }
            result.append(userId);
        }
        
        return result.toString();
        
        // More efficient way (for AI to suggest):
        // return userIds.stream().collect(Collectors.joining("|"));
    }
}