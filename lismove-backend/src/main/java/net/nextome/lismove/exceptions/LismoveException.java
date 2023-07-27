package net.nextome.lismove.exceptions;

import org.springframework.http.HttpStatus;

public class LismoveException extends RuntimeException {

	private static final long serialVersionUID = 1789413647946779827L;
	private HttpStatus httpStatus = null;

	public LismoveException(String message) {
		super(message);
		this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public LismoveException(String message, HttpStatus httpStatus){
		super(message);
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

}
