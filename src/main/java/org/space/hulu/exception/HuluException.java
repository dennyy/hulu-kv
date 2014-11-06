package org.space.hulu.exception;

public class HuluException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HuluException() {
	}

	public HuluException(String message) {
		super(message);
	}

	public HuluException(Throwable cause) {
		super(cause);
	}

	public HuluException(String message, Throwable cause) {
		super(message, cause);
	}

}
