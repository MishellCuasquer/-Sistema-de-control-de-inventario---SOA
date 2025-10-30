package com.ferreteria.inventario.soap;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.exception.ArticuloDuplicadoException;
import com.ferreteria.inventario.exception.ArticuloNoEncontradoException;
import com.ferreteria.inventario.exception.ValidationException;
import com.ferreteria.inventario.service.ArticuloService;
import com.ferreteria.inventario.soap.dto.ArticuloRequest;
import com.ferreteria.inventario.soap.dto.ArticuloResponse;
import com.ferreteria.inventario.soap.dto.SoapFaultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.jws.WebService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementación del servicio web SOAP para artículos (RF5, RF6, RNF5).
 * Delega lógica al ArticuloService y maneja mapeo DTO/Entity.
 * Cumple con WSDL 1.1 y SOAP Faults para errores (RF10).
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@Service
@WebService(
        endpointInterface = "com.ferreteria.inventario.soap.ArticuloSoapService",
        name = "ArticuloSoapService",
        targetNamespace = "http://soap.inventario.ferreteria.com/",
        portName = "ArticuloSoapPort",
        serviceName = "ArticuloSoapService"
)
public class ArticuloSoapServiceImpl implements ArticuloSoapService {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloSoapServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ArticuloService articuloService;

    @Autowired
    public ArticuloSoapServiceImpl(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArticuloResponse insertarArticulo(ArticuloRequest request) throws ArticuloSoapFault {
        logger.info("SOAP: Insertando artículo con código {}", request.getCodigo());

        try {
            // Mapeo Request -> Entity
            Articulo articulo = Articulo.builder()
                    .codigo(request.getCodigo())
                    .nombre(request.getNombre())
                    .descripcion(request.getDescripcion())
                    .categoria(request.getCategoria())
                    .precioCompra(request.getPrecioCompra())
                    .precioVenta(request.getPrecioVenta())
                    .stockActual(request.getStockActual())
                    .stockMinimo(request.getStockMinimo())
                    .proveedor(request.getProveedor())
                    .build();

            // Delegar al servicio (RF5, RF2, RF3)
            Articulo saved = articuloService.insertarArticulo(articulo);

            // Mapeo Entity -> Response
            return mapToResponse(saved);

        } catch (ArticuloDuplicadoException e) {
            logger.error("SOAP Fault: Código duplicado {}", request.getCodigo(), e);
            throw createSoapFault("DUPLICADO", e.getMessage(), "Código ya existe");

        } catch (ValidationException e) {
            logger.error("SOAP Fault: Validación fallida para {}", request.getCodigo(), e);
            String errores = (e.getErrores() != null) ? String.join(", ", e.getErrores()) : "Error desconocido";
            throw createSoapFault("VALIDACION", e.getMessage(), "Errores: " + errores);

        } catch (Exception e) {
            logger.error("SOAP Fault: Error inesperado en insertar", e);
            throw createSoapFault("INTERNO", "Error al insertar artículo", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArticuloResponse consultarArticulo(String codigo) throws ArticuloSoapFault {
        logger.info("SOAP: Consultando artículo con código {}", codigo);

        try {
            // Delegar al servicio (RF6, RF4)
            Articulo articulo = articuloService.consultarArticuloPorCodigo(codigo)
                    .orElseThrow(() -> new ArticuloNoEncontradoException("Código: " + codigo));

            // Mapeo Entity -> Response
            return mapToResponse(articulo);

        } catch (ArticuloNoEncontradoException e) {
            logger.error("SOAP Fault: Artículo no encontrado {}", codigo, e);
            throw createSoapFault("NO_ENCONTRADO", e.getMessage(), "Artículo no existe");

        } catch (Exception e) {
            logger.error("SOAP Fault: Error en consulta", e);
            throw createSoapFault("INTERNO", "Error al consultar artículo", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArticuloResponse actualizarArticulo(String codigo, ArticuloRequest request) throws ArticuloSoapFault {
        logger.info("SOAP: Actualizando artículo con código {}", codigo);

        try {
            // Verificar que el código coincida (opcional, para seguridad)
            if (!codigo.equals(request.getCodigo())) {
                throw new ValidationException(List.of("Código en parámetros no coincide con el request"));
            }

            // Mapeo Request -> Entity (con ID de consulta)
            Articulo articuloExistente = articuloService.consultarArticuloPorCodigo(codigo)
                    .orElseThrow(() -> new ArticuloNoEncontradoException("Código: " + codigo));

            Articulo articulo = Articulo.builder()
                    .id(articuloExistente.getId())
                    .codigo(request.getCodigo())
                    .nombre(request.getNombre())
                    .descripcion(request.getDescripcion())
                    .categoria(request.getCategoria())
                    .precioCompra(request.getPrecioCompra())
                    .precioVenta(request.getPrecioVenta())
                    .stockActual(request.getStockActual())
                    .stockMinimo(request.getStockMinimo())
                    .proveedor(request.getProveedor())
                    .build();

            // Delegar al servicio (RF1)
            Articulo updated = articuloService.actualizarArticulo(articulo);

            // Mapeo Entity -> Response
            return mapToResponse(updated);

        } catch (ArticuloNoEncontradoException e) {
            logger.error("SOAP Fault: Artículo no encontrado para update {}", codigo, e);
            throw createSoapFault("NO_ENCONTRADO", e.getMessage(), "Artículo no existe");

        } catch (ArticuloDuplicadoException e) {
            logger.error("SOAP Fault: Código duplicado en update {}", codigo, e);
            throw createSoapFault("DUPLICADO", e.getMessage(), "Código ya existe");

        } catch (ValidationException e) {
            logger.error("SOAP Fault: Validación en update {}", codigo, e);
            String errores = (e.getErrores() != null) ? String.join(", ", e.getErrores()) : "Error desconocido";
            throw createSoapFault("VALIDACION", e.getMessage(), "Errores: " + errores);

        } catch (Exception e) {
            logger.error("SOAP Fault: Error en actualización", e);
            throw createSoapFault("INTERNO", "Error al actualizar artículo", e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verificarStock(String codigo) throws ArticuloSoapFault {
        logger.info("SOAP: Verificando stock para código {}", codigo);

        try {
            Articulo articulo = articuloService.consultarArticuloPorCodigo(codigo)
                    .orElseThrow(() -> new ArticuloNoEncontradoException("Código: " + codigo));

            // RF7: Stock disponible si > 0
            boolean disponible = articulo.getStockActual() > 0;
            logger.debug("Stock disponible para {}: {}", codigo, disponible);
            return disponible;

        } catch (ArticuloNoEncontradoException e) {
            logger.error("SOAP Fault: Artículo no encontrado en verificarStock {}", codigo, e);
            throw createSoapFault("NO_ENCONTRADO", e.getMessage(), "Artículo no existe");
        } catch (Exception e) {
            logger.error("SOAP Fault: Error en verificación de stock", e);
            throw createSoapFault("INTERNO", "Error al verificar stock", e.getMessage());
        }
    }

    /**
     * Mapea Entity a Response DTO
     */
    private ArticuloResponse mapToResponse(Articulo articulo) {
        return ArticuloResponse.builder()
                .id(articulo.getId())
                .codigo(articulo.getCodigo())
                .nombre(articulo.getNombre())
                .descripcion(articulo.getDescripcion())
                .categoria(articulo.getCategoria())
                .precioCompra(articulo.getPrecioCompra())
                .precioVenta(articulo.getPrecioVenta())
                .stockActual(articulo.getStockActual())
                .stockMinimo(articulo.getStockMinimo())
                .proveedor(articulo.getProveedor())
                .fechaRegistro(articulo.getFechaRegistro() != null ?
                        articulo.getFechaRegistro().format(DATE_FORMATTER) : null)
                .fechaActualizacion(articulo.getFechaActualizacion() != null ?
                        articulo.getFechaActualizacion().format(DATE_FORMATTER) : null)
                .activo(articulo.getActivo())
                .tieneStockBajo(articulo.tieneStockBajo())
                .margenGanancia(articulo.calcularMargenGanancia())
                .mensaje("Operación exitosa")
                .build();
    }

    /**
     * Crea una excepción SOAP Fault con info detallada (RF10) - Ahora retorna para throw externo
     */
    private ArticuloSoapFault createSoapFault(String codigoError, String mensaje, String detalle) {
        SoapFaultInfo faultInfo = SoapFaultInfo.builder()
                .codigoError(codigoError)
                .mensaje(mensaje)
                .detalle(detalle)
                .timestamp(LocalDateTime.now().format(DATE_FORMATTER))
                .build();

        ArticuloSoapFault fault = new ArticuloSoapFault(mensaje, faultInfo);
        logger.warn("Creando SOAP Fault: {} - {}", codigoError, mensaje);
        return fault;  // Retorna el fault para throw en el caller
    }
}