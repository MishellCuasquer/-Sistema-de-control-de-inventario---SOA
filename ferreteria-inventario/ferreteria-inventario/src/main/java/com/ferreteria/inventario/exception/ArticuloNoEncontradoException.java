package com.ferreteria.inventario.exception;

/**
 * Excepción lanzada cuando no se encuentra un artículo (RF10)
 */
public class ArticuloNoEncontradoException extends RuntimeException {

    private final String identificador;

    public ArticuloNoEncontradoException(String identificador) {
        super("No se encontró el artículo con identificador: " + identificador);
        this.identificador = identificador;
    }

    public ArticuloNoEncontradoException(String identificador, String mensaje) {
        super(mensaje);
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }
}
