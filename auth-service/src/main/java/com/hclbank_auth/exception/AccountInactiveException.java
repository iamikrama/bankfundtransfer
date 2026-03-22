package com.hclbank_auth.exception;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(String msg) { super(msg); }
}