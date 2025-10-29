package com.ferreteria.inventario.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepción lanzada cuando hay errores de validación (RF3, RF10)
 */
public class ValidationException extends RuntimeException {

    private final List<String> errores;

    public ValidationException(String mensaje) {
        super(mensaje);
        this.errores = new ArrayList<>();
        this.errores.add(mensaje);
    }

    public ValidationException(List<String> errores) {
        super("Errores de validación: " + String.join(", ", errores));
        this.errores = errores;
    }

    public ValidationException(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}