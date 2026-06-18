package com.role.net.gogather.exception.handler;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.role.net.gogather.dto.error.StandardErrorDTO;
import com.role.net.gogather.exception.InvalidDataException;
import com.role.net.gogather.exception.InvalidRequestException;
import com.role.net.gogather.exception.UnauthorizedRequestException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RequestExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<StandardErrorDTO> invalidRequest(
        InvalidRequestException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Requisição invalida",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<StandardErrorDTO> invalidData(
        InvalidDataException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Dado inválido",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UnauthorizedRequestException.class)
    public ResponseEntity<StandardErrorDTO> unauthorizedRequest(
        UnauthorizedRequestException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Requisição não autorizada",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

}
