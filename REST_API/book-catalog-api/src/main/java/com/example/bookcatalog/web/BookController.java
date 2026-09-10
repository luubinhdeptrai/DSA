package com.example.bookcatalog.web;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.dto.BookResponse;
import com.example.bookcatalog.dto.StockRequest;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
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

@RestController
@RequestMapping("/books")
public class BookController {

    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");

    private final BookService bookService;

    public BookController (BookService bookService)
    {
        this.bookService = bookService;
    }

    @GetMapping(produces = "application/json")
    public List<BookResponse> findAll (@RequestParam(name = "author", required = false) String author,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "20") int size)
    {
        if (page < 0 || page > 1_000_000 || size < 1 || size > 100)
        {
            badRequest("page must be 0..1000000 and size must be 1..100");
        }
        String filter = author==null ? null : checkedText(author, "author", 120);

        return bookService.findAll(filter, page, size).stream().map(BookResponse::from).toList();
    }

    @GetMapping(path = "/{id}" , produces = "application/json")
    public BookResponse findById (@PathVariable("id") long id)
    {
        checkId(id);
        return BookResponse.from(bookService.findById(id).orElseThrow(BookController::notFound));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BookResponse> create(@RequestBody BookRequest bookRequest)
    {
        checkBook(bookRequest);

        try {
            Book book = bookService.create(bookRequest.isbn(), bookRequest.title(), bookRequest.author(), bookRequest.price(), bookRequest.stock() );
            URI location = URI.create("/books/" + book.id());
            return ResponseEntity.created(location).body(BookResponse.from(book));
        } catch (DuplicateKeyException e)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN already exists");
        }
    }

    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json" )
    public BookResponse replace (@PathVariable("id") long id, @RequestBody BookRequest request)
    {
        checkId(id);
        checkBook(request);

        try {
            return BookResponse.from(bookService.replace(id, request.isbn(), request.title(), request.author(), request.price(), request.stock()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
        } catch (DuplicateKeyException e)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ISBN already exists");
        }
    }

    @PatchMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public BookResponse  setStock (@PathVariable("id") long id, @RequestBody StockRequest stockRequest)
    {
        checkId(id);
        checkStock(stockRequest.stock());
        return BookResponse.from(bookService.setStock(id, stockRequest.stock()).orElseThrow(BookController::notFound));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable("id") long id)
    {
        if (!bookService.delete(id))
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
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

    private static void checkId(long id) {
        if (id <= 0) {
            badRequest("id must be positive");
        }
    }

    private static void badRequest(String reason) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
    }
}
