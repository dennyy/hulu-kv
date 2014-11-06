package org.space.hulu.exception;

public class FilePointerNoFoundException extends CacheException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FilePointerNoFoundException() {
 	}

	public FilePointerNoFoundException(String message) {
		super(message);
 	}

	public FilePointerNoFoundException(Throwable cause) {
		super(cause);
 	}

	public FilePointerNoFoundException(String message, Throwable cause) {
		super(message, cause);
 	}

}
