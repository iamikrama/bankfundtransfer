package com.hclbank_auth.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String msg) { super(msg); }
}