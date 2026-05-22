package com.sharks.ticketing.exception;

import com.nimbusds.jwt.proc.BadJWTException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TicketExceptionHandler {

	@ExceptionHandler(TicketNotFoundException.class)
	ResponseEntity<ProblemDetail> handleNotFound(TicketNotFoundException ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		detail.setTitle("Not found");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(detail);
	}

	@ExceptionHandler(InsufficientInventoryException.class)
	ResponseEntity<ProblemDetail> handleConflict(InsufficientInventoryException ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				ex.getMessage() != null ? ex.getMessage() : "Insufficient inventory");
		detail.setTitle("Conflict");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(detail);
	}

	@ExceptionHandler({AccessDeniedException.class})
	ResponseEntity<ProblemDetail> handleForbidden(Exception ex) {
		String message = ex.getMessage() != null ? ex.getMessage() : "Forbidden";
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
		detail.setTitle("Forbidden");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail);
	}

	@ExceptionHandler({AuthenticationException.class, BadJWTException.class})
	ResponseEntity<ProblemDetail> handleUnauthorized(Exception ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNAUTHORIZED,
				"Authentication required or token invalid");
		detail.setTitle("Unauthorized");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(detail);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body");
		detail.setTitle("Validation error");
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ProblemDetail> handleBadRequest(IllegalArgumentException ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				ex.getMessage() != null ? ex.getMessage() : "Bad request");
		detail.setTitle("Bad request");
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred");
		detail.setTitle("Server error");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail);
	}
}
