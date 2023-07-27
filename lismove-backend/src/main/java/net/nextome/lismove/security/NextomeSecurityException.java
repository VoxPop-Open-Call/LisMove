package net.nextome.lismove.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.nextome.lismove.exceptions.ExceptionResponse;
import org.springframework.http.HttpStatus;

public class NextomeSecurityException extends RuntimeException {

	public static final String MISSING_FIELD_ERROR = "Missing value";
	private static final long serialVersionUID = 5379739738930409345L;
	private String message;
	private HttpStatus httpStatus;
	private Integer error_code;

	public NextomeSecurityException(String message, HttpStatus httpStatus) {
		super();
		this.message = message;
		this.httpStatus = httpStatus;
		this.error_code = httpStatus.value();
	}

	public NextomeSecurityException(String message, HttpStatus httpStatus, Integer error_code) {
		super();
		this.message = message;
		this.httpStatus = httpStatus;
		this.error_code = error_code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getHttpStatusCode() {
		return this.httpStatus.value();
	}

	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public int getError_code() {
		return this.error_code;
	}

	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(new ExceptionResponse(getHttpStatus(), getMessage()));
		} catch(JsonProcessingException e) {
			return this.getMessage();
		}
	}
}
