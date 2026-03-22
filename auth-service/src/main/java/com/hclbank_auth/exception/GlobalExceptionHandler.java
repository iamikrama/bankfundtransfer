package com.hclbank_auth.exception;

import com.hclbank_auth.dto.ErrorResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        return ResponseEntity.badRequest().body(
                new ErrorResponse(400, "VALIDATION_ERROR",
                        msg, LocalDateTime.now(), "/auth"));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(409, "DUPLICATE_EMAIL",
                        ex.getMessage(), LocalDateTime.now(), "/auth/signup"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCreds(
            InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(401, "INVALID_CREDENTIALS",
                        ex.getMessage(), LocalDateTime.now(), "/auth/login"));
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleInactive(
            AccountInactiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(403, "ACCOUNT_INACTIVE",
                        ex.getMessage(), LocalDateTime.now(), "/auth/login"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500, "INTERNAL_ERROR",
                        "Something went wrong.",
                        LocalDateTime.now(), "/auth"));
    }
}