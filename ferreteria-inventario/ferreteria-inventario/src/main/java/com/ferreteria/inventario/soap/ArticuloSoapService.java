package com.ferreteria.inventario.soap;

import com.ferreteria.inventario.soap.dto.ArticuloRequest;
import com.ferreteria.inventario.soap.dto.ArticuloResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Interfaz del servicio web SOAP para gestión de artículos (RF5, RF6, RNF5)
 * Cumple con estándares WSDL 1.1 y XML Schema
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@WebService(
        name = "ArticuloSoapService",
        targetNamespace = "http://soap.inventario.ferreteria.com/"
)
@SOAPBinding(
        style = SOAPBinding.Style.DOCUMENT,
        use = SOAPBinding.Use.LITERAL
)
public interface ArticuloSoapService {

    /**
     * Operación SOAP para insertar un nuevo artículo (RF5)
     *
     * @param request datos del artículo a insertar
     * @return respuesta con el artículo creado
     * @throws ArticuloSoapFault si hay errores de validación o duplicados (RF10)
     */
    @WebMethod(operationName = "insertarArticulo")
    @WebResult(name = "insertarArticuloResponse")
    ArticuloResponse insertarArticulo(
            @WebParam(name = "articuloRequest") ArticuloRequest request
    ) throws ArticuloSoapFault;

    /**
     * Operación SOAP para consultar un artículo por código (RF6)
     *
     * @param codigo código del artículo a consultar
     * @return respuesta con los datos del artículo
     * @throws ArticuloSoapFault si el artículo no existe (RF10)
     */
    @WebMethod(operationName = "consultarArticulo")
    @WebResult(name = "consultarArticuloResponse")
    ArticuloResponse consultarArticulo(
            @WebParam(name = "codigo") String codigo
    ) throws ArticuloSoapFault;

    /**
     * Operación SOAP para actualizar un artículo existente
     *
     * @param codigo código del artículo a actualizar
     * @param request datos actualizados del artículo
     * @return respuesta con el artículo actualizado
     * @throws ArticuloSoapFault si hay errores
     */
    @WebMethod(operationName = "actualizarArticulo")
    @WebResult(name = "actualizarArticuloResponse")
    ArticuloResponse actualizarArticulo(
            @WebParam(name = "codigo") String codigo,
            @WebParam(name = "articuloRequest") ArticuloRequest request
    ) throws ArticuloSoapFault;

    /**
     * Operación SOAP para verificar disponibilidad de stock
     *
     * @param codigo código del artículo
     * @return true si hay stock disponible
     * @throws ArticuloSoapFault si el artículo no existe
     */
    @WebMethod(operationName = "verificarStock")
    @WebResult(name = "verificarStockResponse")
    boolean verificarStock(
            @WebParam(name = "codigo") String codigo
    ) throws ArticuloSoapFault;
}