package com.example.springpractice.repository;

import java.util.ArrayList;
import java.util.List;

import com.example.springpractice.model.Notification;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final List<Notification> list;

    public InMemoryNotificationRepository()
    {
        list = new ArrayList<Notification>();
    }

    public void save(Notification notification)
    {
        list.add(notification);
    }

    public List<Notification> findAll()
    {
        return List.copyOf(list);
    }
    
}
