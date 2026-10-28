package com.example.bookcatalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import com.example.bookcatalog.repository.BookRepository;
import com.example.bookcatalog.repository.TransactionAuditRepository;
import com.example.bookcatalog.support.TestPostgresConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@Import(TestPostgresConfiguration.class)
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTest {

    private static final String TEST_PASSWORD = "integration-password-123";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AppUserRepository users;

    @Autowired
    BookRepository books;

    @Autowired
    TransactionAuditRepository audits;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void cleanDatabase() {
        audits.deleteAllInBatch();
        books.deleteAllInBatch();
        users.deleteAllInBatch();
    }

    @Test
    void registerLoginMeAndLogout_usesRealDatabaseBackedSessionFlow()
            throws Exception {
        CsrfData registrationCsrf = csrf(null);

        mockMvc.perform(post("/auth/register")
                        .session(registrationCsrf.session())
                        .param(registrationCsrf.parameterName(),
                                registrationCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "session-user",
                                  "password": "integration-password-123"
                                }
                                """))
                .andExpect(status().isCreated());

        AppUser stored = users.findByUsername("session-user").orElseThrow();
        assertThat(stored.getRole()).isEqualTo("USER");
        assertThat(stored.getPasswordHash()).isNotEqualTo(TEST_PASSWORD);
        assertThat(encoder.matches(TEST_PASSWORD, stored.getPasswordHash()))
                .isTrue();

        CsrfData loginCsrf = csrf(registrationCsrf.session());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .session(loginCsrf.session())
                        .param(loginCsrf.parameterName(), loginCsrf.token())
                        .param("username", "session-user")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andExpect(authenticated().withUsername("session-user"))
                .andReturn();
        MockHttpSession session = (MockHttpSession)
                login.getRequest().getSession(false);

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("session-user"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));

        CsrfData logoutCsrf = csrf(session);
        mockMvc.perform(post("/auth/logout")
                        .session(session)
                        .param(logoutCsrf.parameterName(), logoutCsrf.token()))
                .andExpect(status().isNoContent())
                .andExpect(unauthenticated());

        assertThat(session.isInvalid()).isTrue();
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPassword_returnsUnauthorizedWithoutAuthentication()
            throws Exception {
        users.saveAndFlush(new AppUser("wrong-password-user",
                encoder.encode(TEST_PASSWORD), "USER"));
        CsrfData csrf = csrf(null);

        mockMvc.perform(post("/auth/login")
                        .session(csrf.session())
                        .param(csrf.parameterName(), csrf.token())
                        .param("username", "wrong-password-user")
                        .param("password", "definitely-wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void databaseAdminLogin_withFreshCsrf_canCreateBook() throws Exception {
        users.saveAndFlush(new AppUser("integration-admin",
                encoder.encode(TEST_PASSWORD), "ADMIN"));
        CsrfData loginCsrf = csrf(null);
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .session(loginCsrf.session())
                        .param(loginCsrf.parameterName(), loginCsrf.token())
                        .param("username", "integration-admin")
                        .param("password", TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andExpect(authenticated().withRoles("ADMIN"))
                .andReturn();
        MockHttpSession session = (MockHttpSession)
                login.getRequest().getSession(false);
        CsrfData writeCsrf = csrf(session);

        mockMvc.perform(post("/books")
                        .session(session)
                        .header(writeCsrf.headerName(), writeCsrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isbn": "integration-isbn",
                                  "title": "Integration Book",
                                  "author": "Test Author",
                                  "price": 25.00,
                                  "stock": 6
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("integration-isbn"));

        assertThat(books.findAll())
                .singleElement()
                .satisfies(book -> {
                    assertThat(book.getIsbn()).isEqualTo("integration-isbn");
                    assertThat(book.getStock()).isEqualTo(6);
                });
    }

    private CsrfData csrf(MockHttpSession existingSession) throws Exception {
        MockHttpServletRequestBuilder request = get("/auth/csrf");
        if (existingSession != null) {
            request.session(existingSession);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        MockHttpSession session = (MockHttpSession)
                result.getRequest().getSession(false);
        return new CsrfData(
                session,
                JsonPath.read(body, "$.parameterName"),
                JsonPath.read(body, "$.headerName"),
                JsonPath.read(body, "$.token"));
    }

    private record CsrfData(
            MockHttpSession session,
            String parameterName,
            String headerName,
            String token) {
    }
}
