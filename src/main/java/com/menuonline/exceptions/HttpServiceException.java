package com.menuonline.exceptions;

import org.springframework.http.HttpStatus;

import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HttpServiceException extends RuntimeException {

    private final ErrorMessages messageEnum;

    private final HttpStatus status;

    public static HttpServiceException notFound() {
        return new HttpServiceException(ErrorMessages.ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
