package com.role.net.gogather.exception.handler;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.role.net.gogather.exception.UniqueDataAlreadyInUseException;
import com.role.net.gogather.exception.UserNotAGroupMemberException;
import com.role.net.gogather.dto.error.StandardErrorDTO;
import com.role.net.gogather.exception.DataDoesntMatchException;
import com.role.net.gogather.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorDTO> resourceNotFound(
        ResourceNotFoundException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Recurso não encontrado",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UniqueDataAlreadyInUseException.class)
    public ResponseEntity<StandardErrorDTO> uniqueDataAlreadyInUse(
        UniqueDataAlreadyInUseException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Dado único já em uso",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DataDoesntMatchException.class)
    public ResponseEntity<StandardErrorDTO> uniqueDataAlreadyInUse(
        DataDoesntMatchException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Dados não conferem",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

	@ExceptionHandler(UserNotAGroupMemberException.class)
    public ResponseEntity<StandardErrorDTO> userNotAGroupMember(
        UserNotAGroupMemberException e,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorDTO err = new StandardErrorDTO(
            Instant.now(),
            status.value(),
            "Usuário não é membro do grupo",
            e.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }
}
