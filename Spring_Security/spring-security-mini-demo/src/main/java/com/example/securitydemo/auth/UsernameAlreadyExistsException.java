package com.example.securitydemo.auth;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException() {
        super("Username is not available");
    }
}
