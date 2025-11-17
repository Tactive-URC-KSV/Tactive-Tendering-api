package com.ksv.tactive.exception;
public class DuplicateProjectCodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public DuplicateProjectCodeException(String message) {
		super(message);
	}
	public DuplicateProjectCodeException(String message, Throwable cause) {
		super(message, cause);
	}
}
