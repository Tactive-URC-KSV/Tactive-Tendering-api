package com.ksv.tactive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FormAlreadySubmittedException extends RuntimeException {
    public FormAlreadySubmittedException(String message) {
        super(message);
    }
}
