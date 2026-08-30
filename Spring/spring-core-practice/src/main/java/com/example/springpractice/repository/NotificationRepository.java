package com.example.springpractice.repository;

import com.example.springpractice.model.Notification;
import java.util.List;


public interface NotificationRepository {

    void save(Notification notification);

    List<Notification> findAll();
}
