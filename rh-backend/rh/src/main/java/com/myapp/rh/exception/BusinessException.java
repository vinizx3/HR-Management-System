package com.myapp.rh.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message){
        super(message);
    }
}
