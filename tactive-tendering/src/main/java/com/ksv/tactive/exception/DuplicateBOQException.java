package com.ksv.tactive.exception;

import java.util.List;
import java.util.Map;

public class DuplicateBOQException extends RuntimeException {
    private final List<Map<String, Object>> duplicates;

    public DuplicateBOQException(String message, List<Map<String, Object>> duplicates) {
        super(message);
        this.duplicates = duplicates;
    }

    public List<Map<String, Object>> getDuplicates() {
        return duplicates;
    }
}
