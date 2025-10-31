package com.ferreteria.inventario.soap;

import com.ferreteria.inventario.service.ArticuloService;
import com.ferreteria.inventario.soap.dto.ArticuloRequest;
import com.ferreteria.inventario.soap.dto.ArticuloResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para servicios SOAP (RNF10)
 * Valida las operaciones SOAP insertarArticulo y consultarArticulo
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ArticuloSoapServiceTest {

    @Autowired
    private ArticuloSoapServiceImpl articuloSoapService;

    @Autowired
    private ArticuloService articuloService;

    private ArticuloRequest articuloRequestValido;
    private static int testCounter = 0;

    @BeforeEach
    void setUp() {
        testCounter++;
        // Crear request válido con código único para cada test
        articuloRequestValido = ArticuloRequest.builder()
                .codigo("SOAP-TEST-" + String.format("%03d", testCounter))
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
    @Transactional
    void testInsertarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(articuloRequestValido);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(articuloRequestValido.getCodigo(), response.getCodigo());
        assertEquals("Artículo de Prueba SOAP", response.getNombre());
        assertNotNull(response.getMensaje());

        // Verificar que se guardó en la base de datos
        assertTrue(articuloService.existeArticuloConCodigo(articuloRequestValido.getCodigo()));
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código está duplicado")
    @Transactional
    void testInsertarArticuloDuplicadoSOAPFault() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(articuloRequestValido);
        });

        assertNotNull(exception.getFaultInfo());
        assertEquals("DUPLICADO", exception.getFaultInfo().getCodigoError());
        assertTrue(exception.getMessage().contains("Ya existe") ||
                exception.getMessage().contains("ya existe"));
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
                .proveedor("Proveedor Test")
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF3: Debe validar coherencia de precios en SOAP")
    void testInsertarArticuloPreciosIncoherentesSOAP() {
        // Arrange - Precio venta menor que compra
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-PREC-INV")
                .nombre("Artículo Inválido")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("20.00"))
                .precioVenta(new BigDecimal("15.00")) // Menor que compra
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si el nombre está vacío")
    void testInsertarArticuloNombreVacio() {
        // Arrange
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-NOM-VAC")
                .nombre("")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si la categoría está vacía")
    void testInsertarArticuloCategoriaVacia() {
        // Arrange
        ArticuloRequest requestInvalido = ArticuloRequest.builder()
                .codigo("SOAP-CAT-VAC")
                .nombre("Nombre válido")
                .categoria("")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.insertarArticulo(requestInvalido);
        });

        assertEquals("VALIDACION", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE CONSULTA SOAP ====================

    @Test
    @DisplayName("RF6: Debe consultar un artículo correctamente mediante SOAP")
    @Transactional
    void testConsultarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        ArticuloResponse response = articuloSoapService.consultarArticulo(
                articuloRequestValido.getCodigo());

        // Assert
        assertNotNull(response);
        assertEquals(articuloRequestValido.getCodigo(), response.getCodigo());
        assertEquals("Artículo de Prueba SOAP", response.getNombre());
        assertNotNull(response.getMensaje());
        assertNotNull(response.getFechaRegistro());
        assertNotNull(response.getMargenGanancia());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el artículo no existe")
    void testConsultarArticuloNoExistenteSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("CODIGO-INEXISTENTE-999");
        });

        assertEquals("NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
        assertTrue(exception.getMessage().contains("No se encontró") ||
                exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código es nulo")
    void testConsultarArticuloCodigoNuloSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo(null);
        });

        assertEquals("NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault cuando el código está vacío")
    void testConsultarArticuloCodigoVacioSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("");
        });

        assertEquals("NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    @Test
    @DisplayName("RF6: Debe retornar todos los datos del artículo en la consulta")
    @Transactional
    void testConsultarArticuloRetornaTodosLosDatos() throws ArticuloSoapFault {
        // Arrange
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        ArticuloResponse response = articuloSoapService.consultarArticulo(
                articuloRequestValido.getCodigo());

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
    @Transactional
    void testActualizarArticuloSOAPExitoso() throws ArticuloSoapFault {
        // Arrange - Insertar artículo primero
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Crear request con datos actualizados
        ArticuloRequest requestActualizado = ArticuloRequest.builder()
                .codigo(articuloRequestValido.getCodigo())
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
                articuloRequestValido.getCodigo(), requestActualizado);

        // Assert
        assertNotNull(response);
        assertEquals(articuloRequestValido.getCodigo(), response.getCodigo());
        assertEquals("Nombre Actualizado", response.getNombre());
        assertEquals(100, response.getStockActual());
        assertNotNull(response.getMensaje());
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault al actualizar artículo inexistente")
    void testActualizarArticuloNoExistenteSOAPFault() {
        // Arrange - Request con código que coincide con el parámetro inexistente
        ArticuloRequest requestInexistente = ArticuloRequest.builder()
                .codigo("CODIGO-INEXISTENTE-999")
                .nombre("Nombre válido")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.actualizarArticulo("CODIGO-INEXISTENTE-999", requestInexistente);
        });

        assertEquals("NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE VERIFICACIÓN DE STOCK ====================

    @Test
    @DisplayName("Debe verificar stock disponible correctamente mediante SOAP")
    @Transactional
    void testVerificarStockDisponibleSOAP() throws ArticuloSoapFault {
        // Arrange - Insertar artículo con stock
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        boolean tieneStock = articuloSoapService.verificarStock(
                articuloRequestValido.getCodigo());

        // Assert
        assertTrue(tieneStock);
    }

    @Test
    @DisplayName("Debe retornar false cuando no hay stock")
    @Transactional
    void testVerificarStockAgotadoSOAP() throws ArticuloSoapFault {
        // Arrange - Insertar artículo sin stock
        ArticuloRequest requestSinStock = ArticuloRequest.builder()
                .codigo("SOAP-SIN-STOCK-" + testCounter)
                .nombre("Artículo Sin Stock")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(0)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        articuloSoapService.insertarArticulo(requestSinStock);

        // Act
        boolean tieneStock = articuloSoapService.verificarStock(requestSinStock.getCodigo());

        // Assert
        assertFalse(tieneStock);
    }

    @Test
    @DisplayName("RF10: Debe lanzar SOAP Fault al verificar stock de artículo inexistente")
    void testVerificarStockArticuloInexistenteSOAPFault() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.verificarStock("CODIGO-INEXISTENTE-999");
        });

        assertEquals("NO_ENCONTRADO", exception.getFaultInfo().getCodigoError());
    }

    // ==================== PRUEBAS DE RESPUESTA SOAP ====================

    @Test
    @DisplayName("La respuesta SOAP debe incluir alertas de stock bajo")
    @Transactional
    void testRespuestaSOAPIncluyeAlertaStockBajo() throws ArticuloSoapFault {
        // Arrange - Artículo con stock bajo
        ArticuloRequest requestStockBajo = ArticuloRequest.builder()
                .codigo("SOAP-BAJO-" + testCounter)
                .nombre("Artículo Stock Bajo")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(3)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .build();

        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(requestStockBajo);

        // Assert
        assertNotNull(response);
        assertTrue(response.getTieneStockBajo());
    }

    @Test
    @DisplayName("La respuesta SOAP debe incluir margen de ganancia calculado")
    @Transactional
    void testRespuestaSOAPIncluyeMargenGanancia() throws ArticuloSoapFault {
        // Act
        ArticuloResponse response = articuloSoapService.insertarArticulo(articuloRequestValido);

        // Assert
        assertNotNull(response.getMargenGanancia());
        assertTrue(response.getMargenGanancia().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("La respuesta SOAP debe incluir fechas de registro")
    @Transactional
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
    @Transactional
    void testRendimientoInsercionSOAP() throws ArticuloSoapFault {
        // Arrange
        ArticuloRequest request = ArticuloRequest.builder()
                .codigo("SOAP-PERF-INS-" + testCounter)
                .nombre("Artículo Rendimiento")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
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
    @Transactional
    void testRendimientoConsultaSOAP() throws ArticuloSoapFault {
        // Arrange
        articuloSoapService.insertarArticulo(articuloRequestValido);

        // Act
        long inicio = System.currentTimeMillis();
        articuloSoapService.consultarArticulo(articuloRequestValido.getCodigo());
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
            articuloSoapService.consultarArticulo("INEXISTENTE-999");
        });

        assertNotNull(exception.getFaultInfo().getTimestamp());
        assertFalse(exception.getFaultInfo().getTimestamp().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir detalle del error")
    void testSOAPFaultIncluyeDetalle() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE-999");
        });

        assertNotNull(exception.getFaultInfo().getDetalle());
        assertFalse(exception.getFaultInfo().getDetalle().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir código de error")
    void testSOAPFaultIncluyeCodigoError() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE-999");
        });

        assertNotNull(exception.getFaultInfo().getCodigoError());
        assertFalse(exception.getFaultInfo().getCodigoError().isEmpty());
    }

    @Test
    @DisplayName("RF10: El SOAP Fault debe incluir mensaje descriptivo")
    void testSOAPFaultIncluyeMensaje() {
        // Act & Assert
        ArticuloSoapFault exception = assertThrows(ArticuloSoapFault.class, () -> {
            articuloSoapService.consultarArticulo("INEXISTENTE-999");
        });

        assertNotNull(exception.getFaultInfo().getMensaje());
        assertFalse(exception.getFaultInfo().getMensaje().isEmpty());
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    @DisplayName("RF3: Debe validar que el request no sea nulo")
    void testValidarRequestNoNulo() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            articuloSoapService.insertarArticulo(null);
        });

        assertNotNull(exception.getMessage());
    }
}
