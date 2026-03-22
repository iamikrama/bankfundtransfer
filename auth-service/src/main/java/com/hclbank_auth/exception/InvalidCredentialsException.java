package com.hclbank_auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String msg) { super(msg); }
}