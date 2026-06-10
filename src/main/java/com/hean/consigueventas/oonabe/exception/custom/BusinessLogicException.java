package com.hean.consigueventas.oonabe.exception.custom;

// Para errores 400 (Negocio, Stock, etc.)
public class BusinessLogicException extends RuntimeException{
    public BusinessLogicException(String mensaje) { super(mensaje); }
}
