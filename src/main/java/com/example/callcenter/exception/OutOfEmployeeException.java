package com.example.callcenter.exception;

import java.util.concurrent.RejectedExecutionException;

public class OutOfEmployeeException extends RuntimeException {
    public OutOfEmployeeException(String s) {
        super(s);
    }

    public OutOfEmployeeException(RejectedExecutionException ex) {
        super(ex);
    }
}
