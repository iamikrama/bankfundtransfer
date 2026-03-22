package com.hclbank_account.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String msg) { super(msg); }
}
