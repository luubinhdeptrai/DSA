package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.Book;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    
    List<Book> findByAuthor(String author, Pageable pageable);
}
