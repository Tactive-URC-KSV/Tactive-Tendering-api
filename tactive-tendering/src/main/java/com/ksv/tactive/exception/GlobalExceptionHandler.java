package com.ksv.tactive.exception;

import com.ksv.tactive.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse build(String message, HttpStatus status) {
        return ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFound(DataNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(FormAlreadySubmittedException.class)
    public ResponseEntity<ErrorResponse> handleFormAlreadySubmitted(FormAlreadySubmittedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(FileNotSaveException.class)
    public ResponseEntity<ErrorResponse> handleFileError(FileNotSaveException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleJwtError(JwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build("JWT Token has expired", HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEntryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(DuplicateProjectCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProject(DuplicateProjectCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDbError(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build("Duplicate or invalid data", HttpStatus.CONFLICT));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build("User not found", HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build("Unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }
}