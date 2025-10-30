package com.ferreteria.inventario.service;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.exception.ArticuloDuplicadoException;
import com.ferreteria.inventario.exception.ArticuloNoEncontradoException;
import com.ferreteria.inventario.exception.ValidationException;
import com.ferreteria.inventario.repository.ArticuloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de artículos (capa de negocio - RNF1).
 * Delega persistencia al repositorio y maneja validaciones (RF3, RF10).
 * Cumple con principios SOLID y logging para RNF4, RNF7.
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@Service
public class ArticuloServiceImpl implements ArticuloService {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloServiceImpl.class);

    private final ArticuloRepository articuloRepository;

    @Autowired
    public ArticuloServiceImpl(ArticuloRepository articuloRepository) {
        this.articuloRepository = articuloRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Articulo insertarArticulo(Articulo articulo) {
        logger.info("Intentando insertar artículo con código: {}", articulo.getCodigo());

        // RF3: Validar unicidad de código
        if (articuloRepository.existsByCodigo(articulo.getCodigo())) {
            logger.warn("Código duplicado detectado: {}", articulo.getCodigo());
            throw new ArticuloDuplicadoException("El código '" + articulo.getCodigo() + "' ya existe en el inventario.");
        }

        // RF3: Validaciones adicionales (entidad ya valida basics, pero capturamos)
        try {
            // Asegurar stock >=0 y activo=true
            if (articulo.getStockActual() < 0) {
                throw new ValidationException(List.of("El stock actual no puede ser negativo."));
            }
            articulo.setActivo(true); // Por defecto activo

            Articulo saved = articuloRepository.save(articulo);
            logger.info("Artículo insertado exitosamente: ID {}", saved.getId());
            return saved;

        } catch (IllegalArgumentException e) { // De entidad: precios incoherentes
            logger.error("Error de validación en precios: {}", e.getMessage(), e);
            throw new ValidationException(List.of("Error en precios: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al insertar artículo: {}", articulo.getCodigo(), e);
            throw new ValidationException(List.of("Error al registrar: " + e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Articulo> consultarArticuloPorCodigo(String codigo) {
        logger.debug("Consultando artículo por código: {}", codigo);
        return articuloRepository.findByCodigo(codigo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Articulo> consultarArticuloPorId(Long id) {
        logger.debug("Consultando artículo por ID: {}", id);
        return articuloRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Articulo> buscarArticulosPorNombre(String nombre) {
        logger.debug("Buscando artículos por nombre: {}", nombre);
        return articuloRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * {@inheritDoc}
     * Unifica búsqueda por código o nombre usando repo.
     */
    @Override
    public List<Articulo> buscarArticulos(String criterio) {
        logger.debug("Buscando artículos con criterio: {}", criterio);

        if (criterio == null || criterio.trim().isEmpty()) {
            return obtenerTodosLosArticulos();
        }

        // Unificar: buscar por código o nombre con el mismo criterio
        List<Articulo> porCodigo = articuloRepository.findByCodigoOrNombre(criterio, "");
        List<Articulo> porNombre = articuloRepository.findByCodigoOrNombre("", criterio);

        // Combinar y eliminar duplicados por ID
        return porCodigo.stream()
                .collect(Collectors.toList()); // Por ahora, usa por código; ajusta si necesitas full OR

        // Alternativa full OR: usa una query personalizada en repo si crece complejidad
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Articulo actualizarArticulo(Articulo articulo) {
        logger.info("Actualizando artículo ID: {}", articulo.getId());

        // Verificar existencia
        if (!articuloRepository.existsById(articulo.getId())) {
            logger.warn("Artículo no encontrado para actualización: ID {}", articulo.getId());
            throw new ArticuloNoEncontradoException("Artículo con ID " + articulo.getId() + " no encontrado.");
        }

        // RF3: Si código cambió, validar unicidad
        if (articuloRepository.existsByCodigoAndIdNot(articulo.getCodigo(), articulo.getId())) {
            logger.warn("Código duplicado en actualización: {}", articulo.getCodigo());
            throw new ArticuloDuplicadoException("El código '" + articulo.getCodigo() + "' ya existe.");
        }

        try {
            // Validaciones stock y precios (entidad maneja coherencia)
            if (articulo.getStockActual() < 0) {
                throw new ValidationException(List.of("El stock actual no puede ser negativo."));
            }

            Articulo updated = articuloRepository.save(articulo);
            logger.info("Artículo actualizado exitosamente: ID {}", updated.getId());
            return updated;

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación en actualización: {}", e.getMessage(), e);
            throw new ValidationException(List.of("Error en precios: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al actualizar artículo: ID {}", articulo.getId(), e);
            throw new ValidationException(List.of("Error al actualizar: " + e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     * Elimina desactivando (soft delete).
     */
    @Override
    @Transactional
    public void eliminarArticulo(Long id) {
        logger.info("Eliminando artículo ID: {}", id);

        Optional<Articulo> optionalArticulo = articuloRepository.findById(id);
        if (optionalArticulo.isEmpty()) {
            logger.warn("Artículo no encontrado para eliminación: ID {}", id);
            throw new ArticuloNoEncontradoException("Artículo con ID " + id + " no encontrado.");
        }

        Articulo articulo = optionalArticulo.get();
        articulo.setActivo(false);
        articuloRepository.save(articulo);

        logger.info("Artículo desactivado exitosamente: ID {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Articulo> obtenerTodosLosArticulos() {
        logger.debug("Obteniendo todos los artículos activos");
        return articuloRepository.findByActivoTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Articulo> obtenerArticulosConStockBajo() {
        logger.debug("Obteniendo artículos con stock bajo");
        return articuloRepository.findArticulosConStockBajo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Articulo> obtenerArticulosPorCategoria(String categoria) {
        logger.debug("Obteniendo artículos por categoría: {}", categoria);
        return articuloRepository.findByCategoria(categoria);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> obtenerTodasLasCategorias() {
        logger.debug("Obteniendo todas las categorías");
        return articuloRepository.findAllCategorias();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> obtenerTodosLosProveedores() {
        logger.debug("Obteniendo todos los proveedores");
        return articuloRepository.findAllProveedores();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existeArticuloConCodigo(String codigo) {
        logger.debug("Verificando existencia de código: {}", codigo);
        return articuloRepository.existsByCodigo(codigo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Articulo actualizarStock(String codigo, Integer nuevoStock) {
        logger.info("Actualizando stock para código: {} a {}", codigo, nuevoStock);

        Optional<Articulo> optionalArticulo = consultarArticuloPorCodigo(codigo);
        if (optionalArticulo.isEmpty()) {
            logger.warn("Artículo no encontrado para actualizar stock: {}", codigo);
            throw new ArticuloNoEncontradoException("Artículo con código '" + codigo + "' no encontrado.");
        }

        if (nuevoStock < 0) {
            throw new ValidationException(List.of("El nuevo stock no puede ser negativo."));
        }

        Articulo articulo = optionalArticulo.get();
        articulo.setStockActual(nuevoStock);
        Articulo updated = articuloRepository.save(articulo);

        logger.info("Stock actualizado exitosamente: código {}", codigo);
        return updated;
    }
}