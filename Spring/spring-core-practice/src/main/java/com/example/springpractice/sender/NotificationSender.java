package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;

public interface NotificationSender {

    String channel();

    void send(String senderName, Notification notification);
    
}
