package com.example.securitydemo.web;

import com.example.securitydemo.auth.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> duplicateUsername(
            UsernameAlreadyExistsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Registration conflict");

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validationFailure(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );
        problem.setTitle("Invalid request");
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
