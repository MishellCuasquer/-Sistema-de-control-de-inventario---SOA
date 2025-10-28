package com.ferreteria.inventario.service;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.exception.ArticuloDuplicadoException;
import com.ferreteria.inventario.exception.ArticuloNoEncontradoException;
import com.ferreteria.inventario.exception.ValidationException;
import com.ferreteria.inventario.repository.ArticuloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ArticuloService (RNF10)
 * Valida la lógica de negocio de la capa de servicios
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
class ArticuloServiceTest {

    @Mock
    private ArticuloRepository articuloRepository;

    @InjectMocks
    private ArticuloServiceImpl articuloService;

    private Articulo articuloValido;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear artículo válido para pruebas
        articuloValido = Articulo.builder()
                .id(1L)
                .codigo("TEST-001")
                .nombre("Artículo de Prueba")
                .descripcion("Descripción de prueba")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .proveedor("Proveedor Test")
                .activo(true)
                .build();
    }

    // ==================== PRUEBAS DE INSERCIÓN ====================

    @Test
    @DisplayName("RF5: Debe insertar un artículo válido correctamente")
    void testInsertarArticuloValido() {
        // Arrange
        when(articuloRepository.existsByCodigo(anyString())).thenReturn(false);
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloValido);

        // Act
        Articulo resultado = articuloService.insertarArticulo(articuloValido);

        // Assert
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.getCodigo());
        assertEquals("Artículo de Prueba", resultado.getNombre());
        verify(articuloRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción al insertar artículo con código duplicado")
    void testInsertarArticuloDuplicado() {
        // Arrange
        when(articuloRepository.existsByCodigo(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ArticuloDuplicadoException.class, () -> {
            articuloService.insertarArticulo(articuloValido);
        });

        verify(articuloRepository, never()).save(any(Articulo.class));
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si el código está vacío")
    void testInsertarArticuloCodigoVacio() {
        // Arrange
        Articulo articuloInvalido = Articulo.builder()
                .codigo("")
                .nombre("Nombre válido")
                .categoria("Categoría")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            articuloService.insertarArticulo(articuloInvalido);
        });
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si el precio de venta es menor al de compra")
    void testInsertarArticuloPrecioIncoherente() {
        // Arrange
        Articulo articuloInvalido = Articulo.builder()
                .codigo("TEST-002")
                .nombre("Artículo Inválido")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("20.00"))
                .precioVenta(new BigDecimal("15.00")) // Menor que precio de compra
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            articuloService.insertarArticulo(articuloInvalido);
        });
    }

    @Test
    @DisplayName("RF3: Debe lanzar excepción si los precios son negativos")
    void testInsertarArticuloPreciosNegativos() {
        // Arrange
        Articulo articuloInvalido = Articulo.builder()
                .codigo("TEST-003")
                .nombre("Artículo Inválido")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("-10.00"))
                .precioVenta(new BigDecimal("-5.00"))
                .stockActual(50)
                .stockMinimo(10)
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            articuloService.insertarArticulo(articuloInvalido);
        });
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("RF6: Debe consultar un artículo por código correctamente")
    void testConsultarArticuloPorCodigoExistente() {
        // Arrange
        when(articuloRepository.findByCodigo("TEST-001")).thenReturn(Optional.of(articuloValido));

        // Act
        Optional<Articulo> resultado = articuloService.consultarArticuloPorCodigo("TEST-001");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("TEST-001", resultado.get().getCodigo());
        verify(articuloRepository, times(1)).findByCodigo("TEST-001");
    }

    @Test
    @DisplayName("RF6: Debe retornar Optional vacío si el artículo no existe")
    void testConsultarArticuloPorCodigoNoExistente() {
        // Arrange
        when(articuloRepository.findByCodigo("NOEXISTE-001")).thenReturn(Optional.empty());

        // Act
        Optional<Articulo> resultado = articuloService.consultarArticuloPorCodigo("NOEXISTE-001");

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("RF4: Debe buscar artículos por nombre correctamente")
    void testBuscarArticulosPorNombre() {
        // Arrange
        List<Articulo> articulos = Arrays.asList(articuloValido);
        when(articuloRepository.findByNombreContainingIgnoreCase("Prueba")).thenReturn(articulos);

        // Act
        List<Articulo> resultados = articuloService.buscarArticulosPorNombre("Prueba");

        // Assert
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertTrue(resultados.get(0).getNombre().contains("Prueba"));
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN ====================

    @Test
    @DisplayName("RF1: Debe actualizar un artículo existente correctamente")
    void testActualizarArticuloExistente() {
        // Arrange
        when(articuloRepository.existsById(1L)).thenReturn(true);
        when(articuloRepository.findByCodigo("TEST-001")).thenReturn(Optional.of(articuloValido));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloValido);

        // Act
        Articulo resultado = articuloService.actualizarArticulo(articuloValido);

        // Assert
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.getCodigo());
        verify(articuloRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    @DisplayName("RF1: Debe lanzar excepción al actualizar artículo inexistente")
    void testActualizarArticuloNoExistente() {
        // Arrange
        when(articuloRepository.existsById(999L)).thenReturn(false);
        articuloValido.setId(999L);

        // Act & Assert
        assertThrows(ArticuloNoEncontradoException.class, () -> {
            articuloService.actualizarArticulo(articuloValido);
        });
    }

    // ==================== PRUEBAS DE STOCK ====================

    @Test
    @DisplayName("RF7: Debe obtener artículos con stock bajo")
    void testObtenerArticulosConStockBajo() {
        // Arrange
        Articulo articuloStockBajo = Articulo.builder()
                .codigo("BAJO-001")
                .nombre("Artículo Stock Bajo")
                .categoria("Pruebas")
                .precioCompra(new BigDecimal("10.00"))
                .precioVenta(new BigDecimal("15.00"))
                .stockActual(3)
                .stockMinimo(10)
                .activo(true)
                .build();

        List<Articulo> articulos = Arrays.asList(articuloStockBajo);
        when(articuloRepository.findArticulosConStockBajo()).thenReturn(articulos);

        // Act
        List<Articulo> resultados = articuloService.obtenerArticulosConStockBajo();

        // Assert
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
        assertTrue(resultados.get(0).tieneStockBajo());
    }

    @Test
    @DisplayName("RF7: Debe actualizar el stock de un artículo")
    void testActualizarStock() {
        // Arrange
        when(articuloRepository.findByCodigo("TEST-001")).thenReturn(Optional.of(articuloValido));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloValido);

        // Act
        Articulo resultado = articuloService.actualizarStock("TEST-001", 100);

        // Assert
        assertNotNull(resultado);
        verify(articuloRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    @DisplayName("RF7: Debe lanzar excepción si el stock es negativo")
    void testActualizarStockNegativo() {
        // Arrange
        when(articuloRepository.findByCodigo("TEST-001")).thenReturn(Optional.of(articuloValido));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            articuloService.actualizarStock("TEST-001", -10);
        });
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    @DisplayName("RF3: Debe verificar correctamente si existe un código")
    void testExisteArticuloConCodigo() {
        // Arrange
        when(articuloRepository.existsByCodigo("TEST-001")).thenReturn(true);

        // Act
        boolean existe = articuloService.existeArticuloConCodigo("TEST-001");

        // Assert
        assertTrue(existe);
    }

    @Test
    @DisplayName("RF1: Debe obtener todos los artículos activos")
    void testObtenerTodosLosArticulos() {
        // Arrange
        List<Articulo> articulos = Arrays.asList(articuloValido);
        when(articuloRepository.findByActivoTrue()).thenReturn(articulos);

        // Act
        List<Articulo> resultados = articuloService.obtenerTodosLosArticulos();

        // Assert
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
    }

    @Test
    @DisplayName("RF1: Debe eliminar (desactivar) un artículo")
    void testEliminarArticulo() {
        // Arrange
        when(articuloRepository.findById(1L)).thenReturn(Optional.of(articuloValido));
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloValido);

        // Act
        articuloService.eliminarArticulo(1L);

        // Assert
        verify(articuloRepository, times(1)).save(any(Articulo.class));
    }

    // ==================== PRUEBAS DE RENDIMIENTO ====================

    @Test
    @DisplayName("RNF2: La inserción debe ejecutarse en menos de 500ms")
    void testRendimientoInsercion() {
        // Arrange
        when(articuloRepository.existsByCodigo(anyString())).thenReturn(false);
        when(articuloRepository.save(any(Articulo.class))).thenReturn(articuloValido);

        // Act
        long inicio = System.currentTimeMillis();
        articuloService.insertarArticulo(articuloValido);
        long duracion = System.currentTimeMillis() - inicio;

        // Assert
        assertTrue(duracion < 500, "La inserción tomó " + duracion + "ms, debe ser < 500ms");
    }

    @Test
    @DisplayName("RNF2: La consulta debe ejecutarse en menos de 500ms")
    void testRendimientoConsulta() {
        // Arrange
        when(articuloRepository.findByCodigo("TEST-001")).thenReturn(Optional.of(articuloValido));

        // Act
        long inicio = System.currentTimeMillis();
        articuloService.consultarArticuloPorCodigo("TEST-001");
        long duracion = System.currentTimeMillis() - inicio;

        // Assert
        assertTrue(duracion < 500, "La consulta tomó " + duracion + "ms, debe ser < 500ms");
    }
}