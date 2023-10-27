package com.example.auto_ria.exceptions;

import com.example.auto_ria.dto.CarDTORequest;
import com.example.auto_ria.enums.EBrand;
import com.example.auto_ria.enums.EModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getStatus());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage(), HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}

class ErrorResponse {
    private final String message;
    private final int statusCode;

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.statusCode = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
