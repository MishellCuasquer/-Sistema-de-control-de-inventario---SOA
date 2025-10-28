package com.ferreteria.inventario.soap;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.service.ArticuloService;
import com.ferreteria.inventario.soap.dto.ArticuloRequest;
import com.ferreteria.inventario.soap.dto.ArticuloResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para servicios SOAP (RNF10)
 * Valida las operaciones SOAP insertarArticulo y consultarArticulo
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@SpringBootTest
@Transactional
class ArticuloSoapServiceTest {

    @Autowired
    private ArticuloSoapServiceImpl articuloSoapService;

    @Autowired
    private ArticuloService articuloService;

    private ArticuloRequest articuloRequestValido;

    @BeforeEach
    void setUp() {
        // Crear request válido para pruebas
        articuloRequestValido = ArticuloRequest.builder()
                .codigo("SOAP-TEST-001")
                .nombre("Artículo de Prueba SOAP")
                .descripcion("Descripción de prueba")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test SOAP")
                .build();
    }

    // ==================== PRUEBAS DE INSERCIÓN SOAP ====================

    @Test
    @DisplayName("RF5: Debe insertar un artículo correctamente mediante SOAP")
    void testInsertarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(articuloRequestValido);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("SOAP-TEST-001", response.getCodigo());
        assertEquals("Artículo de Prueba SOAP", response.getNombre());
        assertEquals("Artículo insertado exitosamente", response.getMensaje());

        // Verificar que se guardó en la base de datos
        assertTrue(articuloService.existeArticuloConCodigo("SOAP-TEST-001"));
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código está duplicado")
    void testInsertarArticuloDuplicadoSOAPFault() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(articuloRequestValido);
        });

        assertNotNull(exception.getFaultInfo());
        assertEquals("ARTICULO_DUPLICADO", exception.getFaultInfo().getCodigoError());
        assertTrue(exception.getMessage().contains("Ya existe un artículo"));
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando faltan datos obligatorios")
    void testInsertarArticuloSinDatosObligatorios() {
        // Arrange - Request sin código
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo(null)
                .nombre("Nombre válido")
                .categoria("Categoría")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF3: Debe validar coherencia de precios en SOAP")
    void testInsertarArticuloPreciosIncoherentesSOAP() {
        // Arrange - Precio venta menor que compra
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-TEST-002")
                .nombre("Artículo Inválido")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("20.00"))
                .precioVenta(new BigDecimal("15.00")) // Menor que compra
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
        assertTrue(exception.getMessage().contains("precio"));
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si el nombre está vacío")
    void testInsertarArticuloNombreVacio() {
        // Arrange
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-TEST-003")
                .nombre("")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si la categoría está vacía")
    void testInsertarArticuloCategoriaVacia() {
        // Arrange
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-TEST-004")
                .nombre("Nombre válido")
                .categoria("")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE CONSULTA SOAP ====================

    @Test
    @DisplayName("RF6: Debe consultar un artículo correctamente mediante SOAP")
    void testConsultarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        ArticuloResponse response = articuloSoapService.consultarArticulo("SOAP-TEST-001");

        // Assert
        assertNotNull(response);
        assertEquals("SOAP-TEST-001", response.getCodigo());
        assertEquals("Artículo de Prueba SOAP", response.getNombre());
        assertEquals("Artículo consultado exitosamente", response.getMensaje());
        assertNotNull(response.getFechaRegistro());
        assertNotNull(response.getMargenGanancia());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el artículo no existe")
    void testConsultarArticuloNoExistenteSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("CODIGO-INEXISTENTE");
        });

        assertEquals("ARTICULO_NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
        assertTrue(exception.getMessage().contains("No se encontró"));
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código es nulo")
    void testConsultarArticuloCodigoNuloSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo(null);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código está vacío")
    void testConsultarArticuloCodigoVacioSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("");
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF6: Debe retornar todos los datos del artículo en la consulta")
    void testConsultarArticuloRetornaTodosLosDatos() throws ArticuloSoapFault {
        // Arrange
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        ArticuloResponse response = articuloSoapService.consultarArticulo("SOAP-TEST-001");

        // Assert
        assertNotNull(response.getId());
        assertNotNull(response.getCodigo());
        assertNotNull(response.getNombre());
        assertNotNull(response.getCategoria());
        assertNotNull(response.getPrecioCompra());
        assertNotNull(response.getPrecioVenta());
        assertNotNull(response.getStockActual());
        assertNotNull(response.getStockMinimo());
        assertNotNull(response.getFechaRegistro());
        assertNotNull(response.getActivo());
        assertNotNull(response.getTieneStockBajo());
        assertNotNull(response.getMargenGanancia());
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN SOAP ====================

    @Test
    @DisplayName("Debe actualizar un artículo correctamente mediante SOAP")
    void testActualizarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Crear request con datos actualizados
        ArticuloRequest requestActualizado = ArticuloRequest.builder()
                .codigo("SOAP-TEST-001")
                .nombre("Nombre Actualizado")
                .descripcion("Nueva descripción")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("12.00"))
                .precioVenta(new BigDecimal("18.00"))
                .stockActual(100)
                .stockMinimo(15)
                .proveedor("Nuevo Proveedor")
                .build();

        // Act
        ArticuloResponse response = articuloSoapService.actualizarArticulo(
                "SOAP-TEST-001", requestActualizado);

        // Assert
        assertNotNull(response);
        assertEquals("SOAP-TEST-001", response.getCodigo());
        assertEquals("Nombre Actualizado", response.getNombre());
        assertEquals(100, response.getStockActual());
        assertEquals("Artículo actualizado exitosamente", response.getMensaje());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault al actualizar artículo inexistente")
    void testActualizarArticuloNoExistenteSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.actualizarArticulo(
                    "CODIGO-INEXISTENTE", articuloRequestValido);
        });

        assertEquals("ARTICULO_NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE STOCK ====================

    @Test
    @DisplayName("Debe verificar stock disponible correctamente mediante SOAP")
    void testVerificarStockDisponibleSOAP() throws ArticuloSoapFault {
        // Arrange - Insertar artículo con stock
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        boolean tieneStock = articuloSoapService.verificarStock("SOAP-TEST-001");

        // Assert
        assertTrue(tieneStock);
    }

    @Test
    @DisplayName("Debe retornar false cuando no hay stock")
    void testVerificarStockAgotadoSOAP() throws ArticuloSoapFault {
        // Arrange - Insertar artículo sin stock
        ArticuloRequest requestSinStock = ArticuloRequest.builder()
                .codigo("SOAP-TEST-SIN-STOCK")
                .nombre("Artículo Sin Stock")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(0)
                .stockMinimo(10)
                .build();

        articuloSoapService.insertarArticulo(requestSinStock);

        // Act
        boolean tieneStock = articuloSoapService.verificarStock("SOAP-TEST-SIN-STOCK");

        // Assert
        assertFalse(tieneStock);
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault al verificar stock de artículo inexistente")
    void testVerificarStockArticuloInexistenteSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.verificarStock("CODIGO-INEXISTENTE");
        });

        assertEquals("ARTICULO_NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE RESPUESTA SOAP ====================

    @Test
    @DisplayName("La respuesta SOAP debe incluir alertas de stock bajo")
    void testRespuestaSOAPIncluyeAlertaStockBajo() throws ArticuloSoapFault {
        // Arrange - Artículo con stock bajo
        ArticuloRequest requestStockBajo = ArticuloRequest.builder()
                .codigo("SOAP-TEST-STOCK-BAJO")
                .nombre("Artículo Stock Bajo")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(3)
                .stockMinimo(10)
                .build();

        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(requestStockBajo);

        // Assert
        assertNotNull(response);
        assertTrue(response.getTieneStockBajo());
    }

    @Test
    @DisplayName("La respuesta SOAP debe incluir margen de ganancia calculado")
    void testRespuestaSOAPIncluyeMargenGanancia() throws ArticuloSoapFault {
        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(articuloRequestValido);

        // Assert
        assertNotNull(response.getMargenGanancia());
        assertTrue(response.getMargenGanancia().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("La respuesta SOAP debe incluir fechas de registro")
    void testRespuestaSOAPIncluyeFechas() throws ArticuloSoapFault {
        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(articuloRequestValido);

        // Assert
        assertNotNull(response.getFechaRegistro());
        assertNotNull(response.getFechaActualizacion());
    }

    // ==================== PRUEBAS DE RENDIMIENTO ====================

    @Test
    @DisplayName("RNF2: La inserción SOAP debe ejecutarse en menos de 500ms")
    void testRendimientoInsercionSOAP() throws ArticuloSoapFault {
        // Arrange
        ArticuloRequest request = ArticuloRequest.builder()
                .codigo("SOAP-PERF-INSERT")
                .nombre("Artículo Rendimiento")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act
        long inicio = System.currentTimeMillis();
        articuloSoapService.insertarArticulo(request);
        long duracion = System.currentTimeMillis() - inicio;

        // Assert
        assertTrue(duracion < 500,
                "La inserción SOAP tomó " + duracion + "ms, debe ser < 500ms");
    }

    @Test
    @DisplayName("RNF2: La consulta SOAP debe ejecutarse en menos de 500ms")
    void testRendimientoConsultaSOAP() throws ArticuloSoapFault {
        // Arrange
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        long inicio = System.currentTimeMillis();
        articuloSoapService.consultarArticulo("SOAP-TEST-001");
        long duracion = System.currentTimeMillis() - inicio;

        // Assert
        assertTrue(duracion < 500,
                "La consulta SOAP tomó " + duracion + "ms, debe ser < 500ms");
    }

    // ==================== PRUEBAS DE FAULT INFO ====================

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir timestamp")
    void testSOAPFaultIncluyeTimestamp() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE");
        });

        assertNotNull(exception.getFaultInfo().getTimestamp());
        assertFalse(exception.getFaultInfo().getTimestamp().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir detalle del error")
    void testSOAPFaultIncluyeDetalle() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE");
        });

        assertNotNull(exception.getFaultInfo().getDetalle());
        assertFalse(exception.getFaultInfo().getDetalle().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir código de error")
    void testSOAPFaultIncluyeCodigoError() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE");
        });

        assertNotNull(exception.getFaultInfo().getCodigoError());
        assertFalse(exception.getFaultInfo().getCodigoError().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir mensaje descriptivo")
    void testSOAPFaultIncluyeMensaje() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE");
        });

        assertNotNull(exception.getFaultInfo().getMensaje());
        assertFalse(exception.getFaultInfo().getMensaje().isEmpty());
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    @DisplayName("RF3: Debe validar que el request no sea nulo")
    void testValidarRequestNoNulo() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(null);
        });

        assertEquals("VALIDACION_ERROR", exception.getFaultInfo().getCodigoError());
    }
}