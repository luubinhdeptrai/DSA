package com.example.springpractice.sender;

import  com.example.springpractice.model.Notification;

import org.springframework.stereotype.Component;

import org.springframework.context.annotation.Primary;

@Component("smsSender")
public class SmsNotificationSender implements NotificationSender {

    public String channel()
    {
        return "SMS";
    }

    public void send(String senderName, Notification notification)
    {
        System.out.println("be sent by SMS + " + senderName);
    }
}
