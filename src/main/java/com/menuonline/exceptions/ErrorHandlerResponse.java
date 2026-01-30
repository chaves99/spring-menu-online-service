package com.menuonline.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ErrorHandlerResponse {

    @ExceptionHandler({ NoResourceFoundException.class })
    public ResponseEntity<ErrorDetail> noResourceFoundException(NoResourceFoundException except) {
        return ResponseEntity.badRequest().body(new ErrorDetail(ErrorMessages.NO_RESOURCE_FOUND));
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<ErrorDetail> genericError(Exception except) {
        return ResponseEntity.internalServerError().body(new ErrorDetail(ErrorMessages.INTERNAL_ERROR));
    }

    @ExceptionHandler({ HttpServiceException.class })
    public ResponseEntity<ErrorDetail> customError(HttpServiceException except) {
        return ResponseEntity
                .status(except.getStatus())
                .body(new ErrorDetail(except.getMessageEnum()));
    }

    public static record ErrorDetail(ErrorMessages message) {
    }

    public static enum ErrorMessages {
        EMAIL_EXISTS,
        ESTABLISHMENT_EXISTS,
        ESTABLISHMENT_NOT_EXISTS,
        INTERNAL_ERROR,
        NO_RESOURCE_FOUND,
        ENTITY_NOT_FOUND,
        PRODUCT_NOT_FOUND;
    }
}
