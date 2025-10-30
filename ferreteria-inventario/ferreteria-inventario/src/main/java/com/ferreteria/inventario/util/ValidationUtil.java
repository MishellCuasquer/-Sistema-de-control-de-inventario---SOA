package com.ferreteria.inventario.util;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para validaciones de negocio (RF3, RNF4)
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
public class ValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Valida que un artículo cumpla con todas las reglas de negocio (RF3)
     *
     * @param articulo artículo a validar
     * @throws ValidationException si hay errores de validación
     */
    public static void validarArticulo(Articulo articulo) {
        List<String> errores = new ArrayList<>();

        // Validar código
        if (articulo.getCodigo() == null || articulo.getCodigo().trim().isEmpty()) {
            errores.add("El código del artículo es obligatorio");
        } else if (articulo.getCodigo().length() < 3 || articulo.getCodigo().length() > 50) {
            errores.add("El código debe tener entre 3 y 50 caracteres");
        } else if (!articulo.getCodigo().matches("^[A-Z0-9-]+$")) {
            errores.add("El código solo puede contener letras mayúsculas, números y guiones");
        }

        // Validar nombre
        if (articulo.getNombre() == null || articulo.getNombre().trim().isEmpty()) {
            errores.add("El nombre del artículo es obligatorio");
        } else if (articulo.getNombre().length() < 3 || articulo.getNombre().length() > 200) {
            errores.add("El nombre debe tener entre 3 y 200 caracteres");
        }

        // Validar categoría
        if (articulo.getCategoria() == null || articulo.getCategoria().trim().isEmpty()) {
            errores.add("La categoría es obligatoria");
        }

        // Validar precios (RF3: precios positivos)
        if (articulo.getPrecioCompra() == null) {
            errores.add("El precio de compra es obligatorio");
        } else if (articulo.getPrecioCompra().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El precio de compra debe ser mayor a 0");
        }

        if (articulo.getPrecioVenta() == null) {
            errores.add("El precio de venta es obligatorio");
        } else if (articulo.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("El precio de venta debe ser mayor a 0");
        }

        // Validar coherencia entre precios (RF3)
        if (articulo.getPrecioCompra() != null && articulo.getPrecioVenta() != null) {
            if (articulo.getPrecioVenta().compareTo(articulo.getPrecioCompra()) < 0) {
                errores.add("El precio de venta debe ser mayor o igual al precio de compra");
            }
        }

        // Validar stock
        if (articulo.getStockActual() == null) {
            errores.add("El stock actual es obligatorio");
        } else if (articulo.getStockActual() < 0) {
            errores.add("El stock actual no puede ser negativo");
        }

        if (articulo.getStockMinimo() == null) {
            errores.add("El stock mínimo es obligatorio");
        } else if (articulo.getStockMinimo() < 0) {
            errores.add("El stock mínimo no puede ser negativo");
        }

        // Si hay errores, lanzar excepción
        if (!errores.isEmpty()) {
            logger.error("Errores de validación para artículo {}: {}", articulo.getCodigo(), errores);
            throw new ValidationException("Errores de validación en el artículo", errores);
        }

        logger.debug("Artículo {} validado correctamente", articulo.getCodigo());
    }

    /**
     * Valida que los precios sean positivos (RF3)
     *
     * @param precioCompra precio de compra
     * @param precioVenta precio de venta
     * @return true si ambos precios son positivos
     */
    public static boolean validarPreciosPositivos(BigDecimal precioCompra, BigDecimal precioVenta) {
        return precioCompra != null && precioVenta != null &&
                precioCompra.compareTo(BigDecimal.ZERO) > 0 &&
                precioVenta.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Valida la coherencia entre precio de compra y venta (RF3)
     *
     * @param precioCompra precio de compra
     * @param precioVenta precio de venta
     * @return true si el precio de venta es mayor o igual al de compra
     */
    public static boolean validarCoherenciaPrecios(BigDecimal precioCompra, BigDecimal precioVenta) {
        if (precioCompra == null || precioVenta == null) {
            return false;
        }
        return precioVenta.compareTo(precioCompra) >= 0;
    }

    /**
     * Valida el formato del código del artículo
     *
     * @param codigo código a validar
     * @return true si el código tiene formato válido
     */
    public static boolean validarFormatoCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        return codigo.matches("^[A-Z0-9-]+$") &&
                codigo.length() >= 3 &&
                codigo.length() <= 50;
    }

    /**
     * Valida que el stock actual no sea negativo
     *
     * @param stock stock a validar
     * @return true si el stock es válido
     */
    public static boolean validarStock(Integer stock) {
        return stock != null && stock >= 0;
    }

    /**
     * Verifica si un artículo necesita reabastecimiento (RF7)
     *
     * @param stockActual stock actual
     * @param stockMinimo stock mínimo
     * @return true si el stock actual es menor al mínimo
     */
    public static boolean necesitaReabastecimiento(Integer stockActual, Integer stockMinimo) {
        return stockActual != null && stockMinimo != null && stockActual < stockMinimo;
    }

    /**
     * Normaliza el código del artículo (convierte a mayúsculas y elimina espacios)
     *
     * @param codigo código a normalizar
     * @return código normalizado
     */
    public static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        return codigo.trim().toUpperCase();
    }

    /**
     * Normaliza el nombre del artículo (elimina espacios extras)
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado
     */
    public static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        return nombre.trim().replaceAll("\\s+", " ");
    }
}