package com.ferreteria.inventario.soap.dto;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de errores SOAP Fault (RF10, RNF5)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SoapFaultInfo")
public class SoapFaultInfo {

    @XmlElement
    private String codigoError;

    @XmlElement
    private String mensaje;

    @XmlElement
    private String detalle;

    @XmlElement
    private String timestamp;
}