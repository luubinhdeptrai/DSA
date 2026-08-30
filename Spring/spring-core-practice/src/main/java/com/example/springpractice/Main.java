package com.example.springpractice;

import java.time.Clock;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;


import com.example.springpractice.config.AppConfig;
import com.example.springpractice.service.NotificationService;


public class Main {

    public static void main (String[] args)
    {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class) )
        {
            // NotificationService noti = context.getBean(NotificationService.class);
            // noti.sendWithDefault("Binh", "hehe");
            // noti.sendSms("Binh", "hehe");
            // System.out.println(noti.history().size());

            // Clock clock = context.getBean(Clock.class);
            // System.out.println("Clock bean zone: " + clock.getZone());
            NotificationService n1 = context.getBean(NotificationService.class);
            NotificationService n2 = context.getBean(NotificationService.class);
            System.out.println(n1==n2 ? "Equal" : "Not equal");

            StringBuilder s1 = context.getBean("notificationDraft", StringBuilder.class);
            StringBuilder s2 = context.getBean("notificationDraft", StringBuilder.class);
            System.out.println(s1==s2 ? "Equal" : "Not equal");



        }
    }
    
}
