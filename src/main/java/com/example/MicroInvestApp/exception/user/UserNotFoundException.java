// Purpose: Specific exception for when a user cannot be found. Provides clear error messaging.
package com.example.MicroInvestApp.exception.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Long userId) {
    super("User not found with ID: " + userId);
  }
}
