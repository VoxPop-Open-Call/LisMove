package net.nextome.lismove.exceptions;

import org.springframework.http.HttpStatus;

public class ExceptionResponse {

	private Integer status;
	private String error;
	private String message;

	public ExceptionResponse(Integer status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
	}

	public ExceptionResponse(HttpStatus status, String message) {
		this.status = status.value();
		this.error = status.getReasonPhrase();
		this.message = message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
