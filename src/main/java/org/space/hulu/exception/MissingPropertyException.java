package org.space.hulu.exception;

/**
 * Missing property from Configuration file with primary keyword
 *
 * @author Denny Ye
 * @since 2012-6-20
 * @version 1.0
 */
public class MissingPropertyException extends RuntimeException {

	/** */
	private static final long serialVersionUID = -4678173099439466608L;

	public MissingPropertyException(String msg) {
		super(msg);
	}
	
}

