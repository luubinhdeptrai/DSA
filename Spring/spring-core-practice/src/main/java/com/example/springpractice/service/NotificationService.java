package com.example.springpractice.service;

import com.example.springpractice.model.Notification;

import com.example.springpractice.sender.NotificationSender;

import com.example.springpractice.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;



import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Service
public class NotificationService {
    private final NotificationSender defaultSender;
    private final NotificationRepository repository;
    private final NotificationSender smsSender;
    private final Clock clock;
    private final String senderName;

    public NotificationService(
            // TODO: constructor parameters
            NotificationSender sender,
            NotificationRepository repo,
            @Qualifier("smsSender") NotificationSender sms,
            Clock clo,
            @Value("${notification.sender-name}") String sendername

    ) { 
        // TODO: assign final fields
        defaultSender = sender;
        repository = repo;
        smsSender = sms;
        clock = clo;
        senderName = sendername;
    }

    @PostConstruct
    public void initialize()
    {
        System.out.println("NotificationService initialized: " + senderName);
    }

    @PreDestroy
    public void destroy()
    {
        System.out.println("NotificationService destroyed");
    }

    // public Notification send(String recipient, String message) {
    //     // TODO: create, send, save, return
    //     Notification noti = new Notification(defaultSender.channel(), recipient, message, Instant.now() );

    //     defaultSender.send("Spring Practice App", noti);

    //     repository.save(noti);

    //     return noti;

    // }


    public Notification sendWithDefault(String recipient, String message) {
        // TODO: create, send, save, return
        return createAndSend(defaultSender, recipient, message);


    }


    public Notification sendSms(String recipient, String message) {
        // TODO: create, send, save, return
        return createAndSend(smsSender, recipient, message);

    }

    private Notification createAndSend (NotificationSender sender, String recipient, String message)
    {
        Notification noti = new Notification(sender.channel(), recipient, message, clock.instant());

        //sender.send("Spring Practice App", noti);

        sender.send( senderName,noti);

        repository.save(noti);

        return noti;

    }



    public List<Notification> history() {
        // TODO
        return repository.findAll();
    }
}


