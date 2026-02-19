package com.ksv.tactive.exception;

import com.ksv.tactive.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NO_CONTENT.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(error);
    }
    @ExceptionHandler(FormAlreadySubmittedException.class)
    public ResponseEntity<ErrorResponse> handleFormAlreadySubmittedException(FormAlreadySubmittedException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.ALREADY_REPORTED.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(error);
    }
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFoundException(ProjectNotFoundException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NO_CONTENT.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(error);
    }
    @ExceptionHandler(FileNotSaveException.class)
    public ResponseEntity<ErrorResponse> handleFileNotSaveException(FileNotSaveException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleJwtTokenException(JwtTokenException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntryException(DuplicateEntryException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(DuplicateProjectCodeException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DuplicateProjectCodeException exception) {
        ErrorResponse error = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
