package com.ferreteria.inventario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un artículo del inventario de la ferretería.
 * Implementa las validaciones necesarias según los requisitos funcionales RF2 y RF3.
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@Entity
@Table(name = "articulos",
        indexes = {
                @Index(name = "idx_codigo", columnList = "codigo"),
                @Index(name = "idx_nombre", columnList = "nombre"),
                @Index(name = "idx_categoria", columnList = "categoria")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Articulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código único del artículo (RF3: validación de unicidad)
     */
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "El código del artículo es obligatorio")
    @Size(min = 3, max = 50, message = "El código debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "El código solo puede contener letras mayúsculas, números y guiones")
    private String codigo;

    /**
     * Nombre descriptivo del artículo
     */
    @Column(nullable = false, length = 200)
    @NotBlank(message = "El nombre del artículo es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String nombre;

    /**
     * Descripción detallada del artículo
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Categoría del artículo (RF2)
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    /**
     * Precio de compra del artículo (RF3: debe ser positivo)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio de compra debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal precioCompra;

    /**
     * Precio de venta del artículo (RF3: debe ser positivo y mayor o igual al precio de compra)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio de venta debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal precioVenta;

    /**
     * Stock actual disponible (RF7)
     */
    @Column(nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;

    /**
     * Stock mínimo para alerta (RF7)
     */
    @Column(nullable = false)
    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    /**
     * Proveedor del artículo (RF2)
     */
    @Column(length = 200)
    private String proveedor;

    /**
     * Fecha de registro del artículo
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Fecha de última actualización
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    /**
     * Indica si el artículo está activo
     */
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Valida la coherencia entre precio de compra y venta (RF3)
     * Este método se ejecuta antes de persistir o actualizar la entidad
     */
    @PrePersist
    @PreUpdate
    private void validarCoherenciaPrecios() {
        if (precioVenta != null && precioCompra != null) {
            if (precioVenta.compareTo(precioCompra) < 0) {
                throw new IllegalArgumentException(
                        "El precio de venta debe ser mayor o igual al precio de compra"
                );
            }
        }
    }

    /**
     * Verifica si el artículo tiene stock bajo (RF7)
     *
     * @return true si el stock actual es menor al stock mínimo
     */
    public boolean tieneStockBajo() {
        return stockActual != null && stockMinimo != null && stockActual < stockMinimo;
    }

    /**
     * Calcula el margen de ganancia del artículo
     *
     * @return margen de ganancia en porcentaje
     */
    public BigDecimal calcularMargenGanancia() {
        if (precioCompra == null || precioVenta == null ||
                precioCompra.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal diferencia = precioVenta.subtract(precioCompra);
        return diferencia.divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Calcula el valor total del inventario para este artículo
     *
     * @return valor total (stock * precio de compra)
     */
    public BigDecimal calcularValorInventario() {
        if (stockActual == null || precioCompra == null) {
            return BigDecimal.ZERO;
        }
        return precioCompra.multiply(new BigDecimal(stockActual));
    }

    @Override
    public String toString() {
        return "Articulo{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precioVenta=" + precioVenta +
                ", stockActual=" + stockActual +
                '}';
    }
}