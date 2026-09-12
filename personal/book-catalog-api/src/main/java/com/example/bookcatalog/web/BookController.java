package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.dto.BookResponse;
import com.example.bookcatalog.dto.StockRequest;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import com.example.bookcatalog.model.Book;



@RestController
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;

    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");

    public BookController (BookService bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping (produces = "application/json")
    public List<BookResponse> findAll (@RequestParam(name = "author", required = false) String author,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "20") int size)
    {
        if (page < 0 || page > 1_000_000 || size < 1 || size > 100) {
            badRequest("page must be 0..1000000 and size must be 1..100");
        }
        String filter = author == null ? null : checkedText(author, "author", 120);
        return bookService.findAll(filter, page, size).stream().map(BookResponse::from).toList();
        
    }

    // // C2: 
    // @GetMapping (produces = "application/json")
    // public List<BookResponse> findAll (@RequestParam(name = "author", required = false) String author,
    //                                     @RequestParam(name = "page", defaultValue = "0") int page,
    //                                     @RequestParam(name = "size", defaultValue = "20") int size)
    // {
    //     if (page < 0 || page > 1_000_000 || size < 1 || size > 100) {
    //         badRequest("page must be 0..1000000 and size must be 1..100");
    //     }
    //     String filter = author == null ? null : checkedText(author, "author", 120);
    //     List<Book> bookList = bookService.findAll(filter, page, size);
    //     List<BookResponse> bookResponseList = new ArrayList<BookResponse>();
    //     for(Book book : bookList)
    //     {
    //          bookResponseList.add(BookResponse.from(book));
    //     }
    //      return bookResponseList; 
        
    // }

    @GetMapping(path = "/{id}", produces = "application/json")
    public BookResponse findById (@PathVariable("id") long id)
    {
        checkId(id);
        return BookResponse.from(bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book is not found")));
    }

    // //C2:
    // @GetMapping(path = "/{id}", produces = "application/json")
    // public ResponseEntity<BookResponse> findById (@PathVariable("id") long id)
    // {
    //     checkId(id);

    //     Optional<Book> book = bookService.findById(id);

    //     if (book.isEmpty())
    //     {
    //         return ResponseEntity.notFound().build();
    //     }
    //     return ResponseEntity.ok(BookResponse.from(book.get()));
    // }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookResponse> create (@RequestBody BookRequest request)
    {
        checkBook(request);
        try
        {
            Book book = bookService.create(request.isbn().trim(), request.title().trim(), request.author().trim(), request.price(), request.stock());
            return ResponseEntity.created(URI.create("/books/" + book.id())).body(BookResponse.from(book));

        } catch (DuplicateKeyException e)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ibsn is duplicated");
        }
    }

    private static void checkBook(BookRequest request) {
        checkedText(request.isbn(), "isbn", 20);
        checkedText(request.title(), "title", 200);
        checkedText(request.author(), "author", 120);
        BigDecimal price = request.price();
        if (price == null || price.signum() < 0 || price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {
            badRequest("price must be nonnegative, fit NUMERIC(12,2), and have at most two decimal places");
        }
        checkStock(request.stock());
    }

    private static String checkedText(String value, String field, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            badRequest(field + " is required and must fit its maximum length");
        }
        return value.trim();
    }

    private static void checkStock(Integer stock) {
        if (stock == null || stock < 0) {
            badRequest("stock is required and must be nonnegative");
        }
    }

    private static void checkId (long id)
    {
        if (id <= 0)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must not be negative");
        }
    }

    public static void badRequest (String message)
    {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
    }
}
