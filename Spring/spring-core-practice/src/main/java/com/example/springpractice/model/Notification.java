package com.example.springpractice.model;

import java.time.Instant;

public record Notification (String channel, String recipient, String message, Instant createdAt) {

}
