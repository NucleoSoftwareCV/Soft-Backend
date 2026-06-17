package com.hean.consigueventas.oonabe.common.exception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String token, String message) {
        this(message);
    }

    public TokenRefreshException(String message) {
        super(message);
    }
}
