package net.nextome.lismove.exceptions;

import com.bugsnag.Bugsnag;
import net.nextome.lismove.security.NextomeUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class LismoveExceptionHandler extends ResponseEntityExceptionHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	@Value("${spring.profiles.active:}")
	private String activeProfiles;
	@Autowired
	private Bugsnag bugsnag;

	@ExceptionHandler(value = {LismoveException.class, RuntimeException.class, Exception.class})
	public ResponseEntity<ExceptionResponse> handleTucumException(Exception ex, Authentication authentication, HttpServletRequest request) {

		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		String message;
		boolean sendError = true;

		if(ex instanceof LismoveException) {
			LismoveException lismoveException = (LismoveException) ex;
			message = lismoveException.getMessage();
			httpStatus = (lismoveException.getHttpStatus() == null ? HttpStatus.BAD_REQUEST : lismoveException.getHttpStatus());
			if(message.equalsIgnoreCase("Missing authentication header") || httpStatus.is4xxClientError()) {
				log.error(message);
			} else {
				log.error(message, lismoveException);
			}
			sendError = httpStatus.is5xxServerError();
		} else if(ex instanceof AccessDeniedException) {
			httpStatus = HttpStatus.FORBIDDEN;
			message = ex.getMessage();
			log.error(message, ex);
		} else if(ex instanceof RuntimeException) {
			RuntimeException runtimeException = (RuntimeException) ex;
			message = (runtimeException.getMessage() == null || runtimeException.getMessage().isEmpty() ? "RuntimeExceptionError" : runtimeException.getMessage());
			log.error(message, ex);

		} else {
			message = (ex.getMessage() == null || ex.getMessage().isEmpty() ? "ExceptionError" : ex.getMessage());
			log.error(message, ex);
		}

		String logMessage = httpStatus + ": " + message;
		if(sendError && !activeProfiles.equalsIgnoreCase("local")) {
			this.log.error(logMessage);
			bugsnag.notify(ex, report -> {
				try {
					if(authentication != null && authentication.getPrincipal() != null) {
						NextomeUserDetails user = (NextomeUserDetails) authentication.getPrincipal();
						if(user.getUsername() != null) {
							report.setUser(user.getUserData().getUid(), user.getUserData().getEmail(), user.getUsername());
						}
					}
				} catch(Exception e) {
					log.error("This should not happen", e);
				}
			});
		} else {
			this.log.error(logMessage + " from URL " + request.getRequestURL());
		}

		return new ResponseEntity<>(new ExceptionResponse(httpStatus, message), httpStatus);
	}

}
