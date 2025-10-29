package com.ferreteria.inventario.repository;

import com.ferreteria.inventario.entity.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de artículos en la base de datos.
 * Implementa la capa de persistencia según RF8.
 * Cumple con RNF2 mediante índices y consultas optimizadas.
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    /**
     * Busca un artículo por su código único (RF4)
     *
     * @param codigo código del artículo
     * @return Optional con el artículo si existe
     */
    Optional<Articulo> findByCodigo(String codigo);

    /**
     * Verifica si existe un artículo con el código dado (RF3)
     *
     * @param codigo código a verificar
     * @return true si existe un artículo con ese código
     */
    boolean existsByCodigo(String codigo);

    /**
     * Busca artículos por nombre (búsqueda parcial) - RF4
     *
     * @param nombre nombre o parte del nombre a buscar
     * @return lista de artículos que coinciden
     */
    @Query("SELECT a FROM Articulo a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND a.activo = true")
    List<Articulo> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca artículos por código o nombre (RF4)
     *
     * @param codigo código a buscar
     * @param nombre nombre a buscar
     * @return lista de artículos que coinciden
     */
    @Query("SELECT a FROM Articulo a WHERE " +
            "(LOWER(a.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')) OR " +
            "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND a.activo = true")
    List<Articulo> findByCodigoOrNombre(
            @Param("codigo") String codigo,
            @Param("nombre") String nombre
    );

    /**
     * Obtiene artículos por categoría
     *
     * @param categoria categoría a buscar
     * @return lista de artículos de esa categoría
     */
    List<Articulo> findByCategoria(String categoria);

    /**
     * Obtiene artículos con stock bajo (RF7)
     *
     * @return lista de artículos con stock menor al mínimo
     */
    @Query("SELECT a FROM Articulo a WHERE a.stockActual < a.stockMinimo AND a.activo = true")
    List<Articulo> findArticulosConStockBajo();

    /**
     * Obtiene artículos activos
     *
     * @return lista de artículos activos
     */
    List<Articulo> findByActivoTrue();

    /**
     * Busca artículos por proveedor
     *
     * @param proveedor nombre del proveedor
     * @return lista de artículos de ese proveedor
     */
    List<Articulo> findByProveedor(String proveedor);

    /**
     * Cuenta artículos por categoría
     *
     * @param categoria categoría a contar
     * @return número de artículos en esa categoría
     */
    long countByCategoria(String categoria);

    /**
     * Obtiene todas las categorías distintas
     *
     * @return lista de categorías únicas
     */
    @Query("SELECT DISTINCT a.categoria FROM Articulo a WHERE a.activo = true ORDER BY a.categoria")
    List<String> findAllCategorias();

    /**
     * Obtiene todos los proveedores distintos
     *
     * @return lista de proveedores únicos
     */
    @Query("SELECT DISTINCT a.proveedor FROM Articulo a WHERE a.activo = true AND a.proveedor IS NOT NULL ORDER BY a.proveedor")
    List<String> findAllProveedores();

    /**
     * Busca artículos con stock mayor a cero
     *
     * @return lista de artículos disponibles
     */
    @Query("SELECT a FROM Articulo a WHERE a.stockActual > 0 AND a.activo = true")
    List<Articulo> findArticulosDisponibles();

    /**
     * Verifica si existe un artículo con el código dado, excluyendo un ID específico (RF3 para updates)
     *
     * @param codigo código a verificar
     * @param id ID a excluir (para validación en actualizaciones)
     * @return true si existe otro artículo con ese código (diferente al ID)
     */
    boolean existsByCodigoAndIdNot(String codigo, Long id);
}