package com.ferreteria.inventario.service;

import com.ferreteria.inventario.entity.Articulo;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de gestión de artículos (RF1)
 * Define las operaciones de negocio para el inventario
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
public interface ArticuloService {

    /**
     * Registra un nuevo artículo en el inventario (RF2, RF5)
     *
     * @param articulo artículo a registrar
     * @return artículo registrado con ID asignado
     * @throws com.ferreteria.inventario.exception.ArticuloDuplicadoException si el código ya existe
     * @throws com.ferreteria.inventario.exception.ValidationException si hay errores de validación
     */
    Articulo insertarArticulo(Articulo articulo);

    /**
     * Consulta un artículo por su código (RF4, RF6)
     *
     * @param codigo código del artículo
     * @return Optional con el artículo si existe
     */
    Optional<Articulo> consultarArticuloPorCodigo(String codigo);

    /**
     * Consulta un artículo por su ID
     *
     * @param id identificador del artículo
     * @return Optional con el artículo si existe
     */
    Optional<Articulo> consultarArticuloPorId(Long id);

    /**
     * Busca artículos por nombre (RF4)
     *
     * @param nombre nombre o parte del nombre a buscar
     * @return lista de artículos que coinciden
     */
    List<Articulo> buscarArticulosPorNombre(String nombre);

    /**
     * Busca artículos por código o nombre (RF4)
     *
     * @param criterio criterio de búsqueda
     * @return lista de artículos que coinciden
     */
    List<Articulo> buscarArticulos(String criterio);

    /**
     * Actualiza un artículo existente (RF1)
     *
     * @param articulo artículo con datos actualizados
     * @return artículo actualizado
     * @throws com.ferreteria.inventario.exception.ArticuloNoEncontradoException si no existe
     */
    Articulo actualizarArticulo(Articulo articulo);

    /**
     * Elimina (desactiva) un artículo
     *
     * @param id identificador del artículo
     * @throws com.ferreteria.inventario.exception.ArticuloNoEncontradoException si no existe
     */
    void eliminarArticulo(Long id);

    /**
     * Obtiene todos los artículos activos
     *
     * @return lista de todos los artículos activos
     */
    List<Articulo> obtenerTodosLosArticulos();

    /**
     * Obtiene artículos con stock bajo (RF7)
     *
     * @return lista de artículos con stock menor al mínimo
     */
    List<Articulo> obtenerArticulosConStockBajo();

    /**
     * Obtiene artículos por categoría
     *
     * @param categoria categoría a buscar
     * @return lista de artículos de esa categoría
     */
    List<Articulo> obtenerArticulosPorCategoria(String categoria);

    /**
     * Obtiene todas las categorías disponibles
     *
     * @return lista de categorías únicas
     */
    List<String> obtenerTodasLasCategorias();

    /**
     * Obtiene todos los proveedores disponibles
     *
     * @return lista de proveedores únicos
     */
    List<String> obtenerTodosLosProveedores();

    /**
     * Verifica si existe un artículo con el código dado (RF3)
     *
     * @param codigo código a verificar
     * @return true si existe un artículo con ese código
     */
    boolean existeArticuloConCodigo(String codigo);

    /**
     * Actualiza el stock de un artículo
     *
     * @param codigo código del artículo
     * @param nuevoStock nuevo valor de stock
     * @return artículo actualizado
     * @throws com.ferreteria.inventario.exception.ArticuloNoEncontradoException si no existe
     */
    Articulo actualizarStock(String codigo, Integer nuevoStock);
}