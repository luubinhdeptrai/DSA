package com.example.taskapi.repository;

import com.example.taskapi.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A plain Java interface. We write zero SQL and zero implementation code -
 * Spring Data JPA generates a working implementation of this interface at
 * runtime (a dynamic proxy) and registers it as a Bean.
 *
 * JpaRepository<Task, Long> uses Java generics: Task is the entity type,
 * Long is the type of that entity's @Id field. That alone is enough for
 * Spring Data to generate save(), findAll(), findById(), deleteById(), etc.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
