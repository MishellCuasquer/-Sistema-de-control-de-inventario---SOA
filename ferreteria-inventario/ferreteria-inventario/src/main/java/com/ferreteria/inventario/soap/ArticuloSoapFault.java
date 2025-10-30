package com.ferreteria.inventario.soap;

import com.ferreteria.inventario.soap.dto.SoapFaultInfo;
import jakarta.xml.ws.WebFault;

/**
 * Excepción personalizada para SOAP Faults (RF10, RNF5).
 * Cumple con estándares JAX-WS para propagar errores en WSDL.
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@WebFault(
        name = "ArticuloSoapFault",
        targetNamespace = "http://soap.inventario.ferreteria.com/",
        faultBean = "com.ferreteria.inventario.soap.dto.SoapFaultInfo"
)
public class ArticuloSoapFault extends Exception {

    private final SoapFaultInfo faultInfo;

    /**
     * Constructor principal con mensaje y detail.
     */
    public ArticuloSoapFault(String message, SoapFaultInfo faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * Constructor con mensaje, detail y causa.
     */
    public ArticuloSoapFault(String message, SoapFaultInfo faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * Retorna el fault bean para serialización en XML (estándar JAX-WS).
     */
    public SoapFaultInfo getFaultInfo() {
        return faultInfo;
    }

    /**
     * Getter para el info del fault (serializado en XML).
     */
    public SoapFaultInfo getFaultInfoDetail() {
        return faultInfo;
    }
}