package org.space.hulu.exception;

public class FileContentDeletedException extends CacheException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FileContentDeletedException() {
 	}

	public FileContentDeletedException(String message) {
		super(message);
 	}

	public FileContentDeletedException(Throwable cause) {
		super(cause);
 	}

	public FileContentDeletedException(String message, Throwable cause) {
		super(message, cause);
 	}

}
