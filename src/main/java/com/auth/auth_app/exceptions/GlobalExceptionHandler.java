package com.auth.auth_app.exceptions;

import com.auth.auth_app.models.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> ResourceNotFoundExceptionHandler(ResourceNotFoundException exception)
    {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), HttpStatus.NOT_FOUND, LocalTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    @ExceptionHandler(EmailAlreadyExit.class)
    public ResponseEntity<ErrorMessage> EmailAlreadyExitsHandler(EmailAlreadyExit exception)
    {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), HttpStatus.NOT_ACCEPTABLE, LocalTime.now());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorMessage);
    }

    @ExceptionHandler(UserNotFoundWithNameException.class)
    public ResponseEntity<ErrorMessage> UserNotFoundWithNameExceptionHandler(UserNotFoundWithNameException exception)
    {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), HttpStatus.NOT_FOUND, LocalTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    @ExceptionHandler(UserIdIsNotValid.class)
    public ResponseEntity<ErrorMessage> UserIdIsNotValidHandler(UserIdIsNotValid exception)
    {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), HttpStatus.NOT_ACCEPTABLE, LocalTime.now());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> ArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException exception)
    {
        ErrorMessage errorMessage = new ErrorMessage("Passed Id is not valid Pleases Check once again", HttpStatus.NOT_FOUND, LocalTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }
}
