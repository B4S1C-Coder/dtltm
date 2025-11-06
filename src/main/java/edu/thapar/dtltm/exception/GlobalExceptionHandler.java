package edu.thapar.dtltm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.thapar.dtltm.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorDTO> handleResourceNotFound(
    ResourceNotFoundException ex, HttpServletRequest request
  ) {

    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.NOT_FOUND.value(),
      HttpStatus.NOT_FOUND.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiErrorDTO> handleBadRequest(
    BadRequestException ex, HttpServletRequest request
  ) {

    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.BAD_REQUEST.value(),
      HttpStatus.BAD_REQUEST.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiErrorDTO> handleConflict(
    ConflictException ex, HttpServletRequest request
  ) {
    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.CONFLICT.value(),
      HttpStatus.CONFLICT.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiErrorDTO> handleForbidden(
    ForbiddenException ex, HttpServletRequest request
  ) {
    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.FORBIDDEN.value(),
      HttpStatus.FORBIDDEN.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorDTO> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request
  ) {
    String errorMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.BAD_REQUEST.value(),
      HttpStatus.BAD_REQUEST.getReasonPhrase(),
      errorMessage,
      request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorDTO> handleGenericException(
    Exception ex, HttpServletRequest request
  ) {

    ApiErrorDTO error = new ApiErrorDTO(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
      "Uncaught exception occurred",
      request.getRequestURI()
    );

    ex.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
