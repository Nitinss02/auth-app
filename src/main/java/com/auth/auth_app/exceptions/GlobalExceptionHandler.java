package com.auth.auth_app.exceptions;

import com.auth.auth_app.models.ErrorMessage;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.rmi.ServerException;
import java.time.LocalDate;
import java.time.LocalTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialsExpiredException.class,
            DisabledException.class
    })
    public ResponseEntity<ErrorMessage> HandleAuthException(Exception e, HttpServletRequest request){
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage(), HttpStatus.BAD_REQUEST, LocalTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

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
