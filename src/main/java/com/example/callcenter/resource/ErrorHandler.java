package com.example.callcenter.resource;

import com.example.callcenter.model.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(OutOfMemoryError.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Error handle(OutOfMemoryError ex) {
        return Error.builder()
                .message(ex.getMessage())
                .cause(ex.getCause().getClass().getSimpleName())
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Error handle(Throwable ex) {
        log.error("This is an unknown exception", ex);
        return Error.builder()
                .message(ex.getMessage())
                .cause(ex.getCause().getClass().getSimpleName())
                .build();
    }
}
