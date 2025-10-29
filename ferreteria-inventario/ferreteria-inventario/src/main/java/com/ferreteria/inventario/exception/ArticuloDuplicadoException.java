package com.ferreteria.inventario.exception;

/**
 * Excepción lanzada cuando se intenta registrar un artículo con código duplicado (RF3, RF10)
 */
public class ArticuloDuplicadoException extends RuntimeException {

    private final String codigo;

    public ArticuloDuplicadoException(String codigo) {
        super("Ya existe un artículo con el código: " + codigo);
        this.codigo = codigo;
    }

    public ArticuloDuplicadoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
