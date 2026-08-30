package com.example.springpractice.sender;

import com.example.springpractice.model.Notification;
import org.springframework.stereotype.Component;

import org.springframework.context.annotation.Primary;

@Primary
@Component("emailSender")
public class EmailNotificationSender implements NotificationSender {

    public String channel()
    {
        return "EMAIL";
    }

    public void send(String senderName, Notification notification)
    {
        System.out.println("be sent by EMAIL + " + senderName);
    }
    
}
