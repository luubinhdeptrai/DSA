package com.example.bookcatalog.security;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.model.Book;
import com.example.bookcatalog.service.BookService;
import com.example.bookcatalog.service.RegistrationService;
import com.example.bookcatalog.web.AuthController;
import com.example.bookcatalog.web.BookController;
import com.example.bookcatalog.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bookcatalog.security.TokenService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = {BookController.class, AuthController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, GlobalExceptionHandler.class})
class SecurityPolicyWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @MockitoBean
    RegistrationService registrationService;

    @MockitoBean
    UserDetailsService userDetailsService;

    @MockitoBean
    TokenService tokenService;

    @Test
    void booksGet_anonymous_isPermitted() throws Exception {
        when(bookService.findAll(null, 0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    void me_anonymous_returnsUnauthorizedProblem() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void create_anonymousWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    void create_anonymousWithCsrf_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_userWithCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithCsrf_reachesController() throws Exception {
        when(bookService.create("security-isbn", "Title", "Author",
                new BigDecimal("12.50"), 4))
                .thenReturn(book(31L));

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBookJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(31));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void create_adminWithInvalidCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/books").with(csrf().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content(validBookJson()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(bookService);
    }

    private static Book book(long id) {
        Book book = new Book("security-isbn", "Title", "Author",
                new BigDecimal("12.50"), 4);
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }

    private static String validBookJson() {
        return """
                {
                  "isbn": "security-isbn",
                  "title": "Title",
                  "author": "Author",
                  "price": 12.50,
                  "stock": 4
                }
                """;
    }
}
