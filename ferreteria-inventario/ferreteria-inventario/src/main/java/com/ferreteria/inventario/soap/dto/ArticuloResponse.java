package com.ferreteria.inventario.soap.dto;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de consulta de artículo (RF6)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArticuloResponse", propOrder = {
        "id", "codigo", "nombre", "descripcion", "categoria",
        "precioCompra", "precioVenta", "stockActual", "stockMinimo",
        "proveedor", "fechaRegistro", "fechaActualizacion", "activo",
        "tieneStockBajo", "margenGanancia", "mensaje"
})
@XmlRootElement(name = "ArticuloResponse")
public class ArticuloResponse {

    @XmlElement
    private Long id;

    @XmlElement
    private String codigo;

    @XmlElement
    private String nombre;

    @XmlElement
    private String descripcion;

    @XmlElement
    private String categoria;

    @XmlElement
    private BigDecimal precioCompra;

    @XmlElement
    private BigDecimal precioVenta;

    @XmlElement
    private Integer stockActual;

    @XmlElement
    private Integer stockMinimo;

    @XmlElement
    private String proveedor;

    @XmlElement
    private String fechaRegistro;

    @XmlElement
    private String fechaActualizacion;

    @XmlElement
    private Boolean activo;

    @XmlElement
    private Boolean tieneStockBajo;

    @XmlElement
    private BigDecimal margenGanancia;

    @XmlElement
    private String mensaje;
}
