package com.somapay.transfer_api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception) {
		List<String> errors = exception.getBindingResult().getAllErrors().stream()
				.map(GlobalExceptionHandler::formatValidationError)
				.toList();

		ProblemDetail problem = createProblem(HttpStatus.BAD_REQUEST, "Request validation failed");
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> handleConstraintViolation(
			ConstraintViolationException exception) {
		List<String> errors = exception.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.sorted()
				.toList();

		ProblemDetail problem = createProblem(HttpStatus.BAD_REQUEST, "Request validation failed");
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException exception) {
		return response(HttpStatus.BAD_REQUEST, exception.getMessage(), "Invalid request");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ProblemDetail> handleUnreadableMessage() {
		return response(HttpStatus.BAD_REQUEST, "Malformed request body", "Invalid request");
	}

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleAccountNotFound(AccountNotFoundException exception) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage(), "Account not found");
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ProblemDetail> handleOptimisticLockingFailure() {
		return response(HttpStatus.CONFLICT,
				"The resource was modified by another request. Please retry.", "Conflict");
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ProblemDetail> handleInsufficientBalance(
			InsufficientBalanceException exception) {
		return response(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(),
				"Insufficient balance");
	}

	private static String formatValidationError(ObjectError error) {
		String message = error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage();
		if (error instanceof FieldError fieldError) {
			return fieldError.getField() + ": " + message;
		}
		return message;
	}

	private static ResponseEntity<ProblemDetail> response(
			HttpStatus status, String detail, String fallbackDetail) {
		String resolvedDetail = detail == null || detail.isBlank() ? fallbackDetail : detail;
		return ResponseEntity.status(status).body(createProblem(status, resolvedDetail));
	}

	private static ProblemDetail createProblem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}
}
