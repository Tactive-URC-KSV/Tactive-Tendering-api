package com.ksv.tactive.exception;


public class DuplicateEntryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateEntryException(String message) {
		super(message);
	}

	public DuplicateEntryException(String message, Throwable cause) {
		super(message, cause);
	}

}
