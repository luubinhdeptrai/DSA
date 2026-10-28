package com.example.bookcatalog.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookRequestBoundaryTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (factory != null) factory.close();
    }

    static Stream<Arguments> isbnCases() {
        return Stream.of(
                Arguments.of("null", null, false),
                Arguments.of("empty", "", false),
                Arguments.of("blank", "   ", false),
                Arguments.of("length 1", "a", true),
                Arguments.of("length 19", "a".repeat(19), true),
                Arguments.of("length 20", "a".repeat(20), true),
                Arguments.of("length 21", "a".repeat(21), false),
                Arguments.of("padded 20", "  " + "a".repeat(20) + "  ", true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("isbnCases")
    void isbn_obeysDeclaredConstraints(String caseName, String input, boolean expectedValid) {
        BookRequest request = new BookRequest(input, "Title", "Author",
                new BigDecimal("1.00"), 1);
        boolean isbnValid = validator.validate(request).stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("isbn"));
        assertEquals(expectedValid, isbnValid, caseName);
    }
}
