package com.ferreteria.inventario.soap.dto;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para solicitud de inserción/actualización de artículo (RF5)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArticuloRequest", propOrder = {
        "codigo", "nombre", "descripcion", "categoria",
        "precioCompra", "precioVenta", "stockActual", "stockMinimo", "proveedor"
})
public class ArticuloRequest {

    @XmlElement(required = true)
    private String codigo;

    @XmlElement(required = true)
    private String nombre;

    @XmlElement
    private String descripcion;

    @XmlElement(required = true)
    private String categoria;

    @XmlElement(required = true)
    private BigDecimal precioCompra;

    @XmlElement(required = true)
    private BigDecimal precioVenta;

    @XmlElement(required = true)
    private Integer stockActual;

    @XmlElement(required = true)
    private Integer stockMinimo;

    @XmlElement
    private String proveedor;
}
