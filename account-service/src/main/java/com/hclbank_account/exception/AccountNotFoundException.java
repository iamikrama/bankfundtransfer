package com.hclbank_account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String msg) { super(msg); }
}