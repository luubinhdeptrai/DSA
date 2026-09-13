package com.example.bookcatalog.web;

import com.example.bookcatalog.exception.BookNotFoundException;
import com.example.bookcatalog.exception.DuplicateIsbnException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_ERROR = "_request";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        addFieldErrors(fieldErrors, exception.getBindingResult().getFieldErrors());
        addGlobalErrors(fieldErrors, exception.getBindingResult().getGlobalErrors());

        ProblemDetail problem = validationProblem(status, fieldErrors);
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (exception.isForReturnValue()) {
            logger.error("Controller return-value validation failed", exception);
            ProblemDetail problem = problem(
                    status,
                    "Internal Server Error",
                    "An unexpected server error occurred.");
            return handleExceptionInternal(
                    exception, problem, headers, status, request);
        }

        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

        for (ParameterErrors beanResult : exception.getBeanResults()) {
            addFieldErrors(fieldErrors, beanResult.getFieldErrors());
            addGlobalErrors(fieldErrors, beanResult.getGlobalErrors());
        }

        for (ParameterValidationResult valueResult : exception.getValueResults()) {
            String parameterName = parameterName(valueResult);
            for (MessageSourceResolvable error : valueResult.getResolvableErrors()) {
                addError(fieldErrors, parameterName, message(error));
            }
        }

        for (MessageSourceResolvable error
                : exception.getCrossParameterValidationResults()) {
            addError(fieldErrors, REQUEST_ERROR, message(error));
        }

        ProblemDetail problem = validationProblem(status, fieldErrors);
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(
                status,
                "Unreadable request body",
                "The request body is missing, malformed, or contains an incompatible JSON value.");
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(
                status,
                "Invalid parameter",
                "A path or query parameter has an incompatible value.");
        return handleExceptionInternal(
                exception, problem, headers, status, request);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Object> handleBookNotFound(
            BookNotFoundException exception,
            WebRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.NOT_FOUND,
                "Book not found",
                exception.getMessage());
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.NOT_FOUND,
                request);
    }

    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Object> handleDuplicateIsbn(
            DuplicateIsbnException exception,
            WebRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.CONFLICT,
                "ISBN conflict",
                exception.getMessage());
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.CONFLICT,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(
            Exception exception,
            WebRequest request) {
        if (exception instanceof ErrorResponse errorResponse) {
            return handleExceptionInternal(
                    exception,
                    errorResponse.getBody(),
                    errorResponse.getHeaders(),
                    errorResponse.getStatusCode(),
                    request);
        }

        logger.error("Unhandled exception while processing an HTTP request", exception);
        ProblemDetail problem = problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected server error occurred.");
        return handleExceptionInternal(
                exception,
                problem,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    private static ProblemDetail validationProblem(
            HttpStatusCode status,
            Map<String, List<String>> fieldErrors) {
        ProblemDetail problem = problem(
                status,
                "Validation failed",
                "One or more request values are invalid.");
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    private static ProblemDetail problem(
            HttpStatusCode status,
            String title,
            String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }

    private static void addFieldErrors(
            Map<String, List<String>> fieldErrors,
            List<FieldError> errors) {
        for (FieldError error : errors) {
            addError(fieldErrors, error.getField(), message(error));
        }
    }

    private static void addGlobalErrors(
            Map<String, List<String>> fieldErrors,
            List<ObjectError> errors) {
        for (ObjectError error : errors) {
            addError(fieldErrors, REQUEST_ERROR, message(error));
        }
    }

    private static void addError(
            Map<String, List<String>> fieldErrors,
            String field,
            String message) {
        List<String> messages = fieldErrors.computeIfAbsent(
                field, ignored -> new ArrayList<>());
        if (!messages.contains(message)) {
            messages.add(message);
        }
    }

    private static String message(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return message == null ? "is invalid" : message;
    }

    private static String parameterName(ParameterValidationResult result) {
        MethodParameter parameter = result.getMethodParameter();

        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            String name = pathVariable.name().isBlank()
                    ? pathVariable.value()
                    : pathVariable.name();
            if (!name.isBlank()) {
                return name;
            }
        }

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            String name = requestParam.name().isBlank()
                    ? requestParam.value()
                    : requestParam.name();
            if (!name.isBlank()) {
                return name;
            }
        }

        String discoveredName = parameter.getParameterName();
        return discoveredName != null
                ? discoveredName
                : "argument" + parameter.getParameterIndex();
    }
}
