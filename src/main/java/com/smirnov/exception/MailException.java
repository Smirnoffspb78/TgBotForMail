package com.smirnov.exception;

public class MailException extends RuntimeException{
    public MailException(String message) {
        super(message);
    }
}
