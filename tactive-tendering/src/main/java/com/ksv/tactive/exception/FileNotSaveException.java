package com.ksv.tactive.exception;
public class FileNotSaveException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public FileNotSaveException(String message) {
		super(message);
	}
	public FileNotSaveException(String message, Throwable cause) {
		super(message, cause);
	}
}
