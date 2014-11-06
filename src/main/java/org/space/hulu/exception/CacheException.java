package org.space.hulu.exception;

public class CacheException extends HuluException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CacheException() {
 	}

	public CacheException(String message) {
		super(message);
 	}

	public CacheException(Throwable cause) {
		super(cause);
 	}

	public CacheException(String message, Throwable cause) {
		super(message, cause);
 	}

}
