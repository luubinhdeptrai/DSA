package com.example.bookcatalog.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void findAll_withQueryParameters_returnsMappedJson() throws Exception {
        when(bookService.findAll("Ursula Le Guin", 0, 2))
                .thenReturn(List.of(book(7L, "isbn-7")));

        mockMvc.perform(get("/books")
                        .param("author", "  Ursula Le Guin  ")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].isbn").value("isbn-7"));
    }

    @Test
    void create_validBody_returnsCreatedLocationAndBody() throws Exception {
        when(bookService.create("isbn-8", "Title", "Author",
                new BigDecimal("12.50"), 4))
                .thenReturn(book(8L, "isbn-8"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson("isbn-8")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/books/8"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.isbn").value("isbn-8"));
    }

    @Test
    void create_invalidBody_returnsActualValidationProblemAndSkipsService()
            throws Exception {
        String invalid = """
                {
                  "isbn": "   ",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """;

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title")
                        .value("some fields failed (title)"))
                .andExpect(jsonPath("$.detail")
                        .value("some field failed (detail)"))
                .andExpect(jsonPath("$.instance").value("/books"))
                .andExpect(jsonPath("$.fieldErrors.isbn[0]")
                        .value("isbn must not be blank"));

        verifyNoInteractions(bookService);
    }

    @Test
    void findById_missingBook_returnsActualNotFoundProblem() throws Exception {
        when(bookService.findById(99L))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/books/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Book not found"))
                .andExpect(jsonPath("$.detail")
                        .value("Book with id: 99 is not found"));
    }

    @Test
    void create_duplicateIsbn_returnsActualConflictProblem() throws Exception {
        when(bookService.create(anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyInt()))
                .thenThrow(new DuplicateIsbnException(
                        new IllegalStateException("test cause")));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson("duplicate")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("ISBN conflict"))
                .andExpect(jsonPath("$.detail").value("Isbn is duplicated"));
    }


    @Test
    void findAll_noMatches_returnsEmptyJsonList() throws Exception {
        when(bookService.findAll(null, 0, 20)).thenReturn(List.of());
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    static Stream<Arguments> invalidBodies() {
        String valid = validBookJson("boundary");
        return Stream.of(
                Arguments.of("long ISBN", valid.replace("boundary", "a".repeat(21)), "isbn"),
                Arguments.of("negative price", valid.replace("12.50", "-0.01"), "price"),
                Arguments.of("negative stock", valid.replace("\"stock\": 4", "\"stock\": -1"), "stock"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBodies")
    void create_invalidField_returnsFieldErrorAndSkipsService(
            String caseName, String body, String field) throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.fieldErrors." + field).isArray());
        verifyNoInteractions(bookService);
    }

    @ParameterizedTest
    @CsvSource({"page,-1,page", "page,1000001,page", "size,0,size", "size,1001,size"})
    void findAll_outOfRangeQuery_rejectsBeforeService(
            String parameter, String value, String field) throws Exception {
        mockMvc.perform(get("/books").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors." + field).isArray());
        verifyNoInteractions(bookService);
    }

    @ParameterizedTest
    @CsvSource({"0,1", "0,1000", "1000000,20"})
    void findAll_validQueryBoundaries_reachService(int page, int size) throws Exception {
        when(bookService.findAll(null, page, size)).thenReturn(List.of());
        mockMvc.perform(get("/books").param("page", Integer.toString(page))
                        .param("size", Integer.toString(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(bookService).findAll(null, page, size);
    }

    @Test
    void create_zeroPriceAndStock_areAllowed() throws Exception {
        when(bookService.create("boundary", "Title", "Author", new BigDecimal("0.00"), 0))
                .thenReturn(book(9L, "boundary"));
        String body = validBookJson("boundary").replace("12.50", "0.00")
                .replace("\"stock\": 4", "\"stock\": 0");
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        verify(bookService).create("boundary", "Title", "Author", new BigDecimal("0.00"), 0);
    }

    @Test
    void create_malformedJson_returnsUnreadableProblem() throws Exception {
        mockMvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isbn\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Unreadable request body"));
        verifyNoInteractions(bookService);
    }

    private static Book book(long id, String isbn) {
        Book book = new Book(isbn, "Title", "Author",
                new BigDecimal("12.50"), 4);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private static String validBookJson(String isbn) {
        return """
                {
                  "isbn": "%s",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """.formatted(isbn);
    }
}
