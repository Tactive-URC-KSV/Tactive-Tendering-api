package com.ksv.tactive.exception;
public class DataNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public DataNotFoundException(String message) {
		super(message);
	}
	public DataNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
