-- ============================================================
-- Script de Creación de Base de Datos - Sistema Inventario Ferretería
-- ============================================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS ferreteria_inventario
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ferreteria_inventario;

-- Eliminar tabla si existe (para desarrollo)
DROP TABLE IF EXISTS articulos;

-- Crear tabla de artículos
CREATE TABLE articulos (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           codigo VARCHAR(50) NOT NULL UNIQUE,
                           nombre VARCHAR(200) NOT NULL,
                           descripcion TEXT,
                           categoria VARCHAR(100) NOT NULL,
                           precio_compra DECIMAL(10, 2) NOT NULL,
                           precio_venta DECIMAL(10, 2) NOT NULL,
                           stock_actual INT NOT NULL DEFAULT 0,
                           stock_minimo INT NOT NULL DEFAULT 5,
                           proveedor VARCHAR(200),
                           fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           activo BOOLEAN DEFAULT TRUE,

    -- Constraints
                           CONSTRAINT chk_precio_compra CHECK (precio_compra > 0),
                           CONSTRAINT chk_precio_venta CHECK (precio_venta > 0),
                           CONSTRAINT chk_precio_coherencia CHECK (precio_venta >= precio_compra),
                           CONSTRAINT chk_stock_actual CHECK (stock_actual >= 0),
                           CONSTRAINT chk_stock_minimo CHECK (stock_minimo >= 0),

    -- Índices para mejorar rendimiento
                           INDEX idx_codigo (codigo),
                           INDEX idx_nombre (nombre),
                           INDEX idx_categoria (categoria),
                           INDEX idx_stock (stock_actual, stock_minimo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar datos de ejemplo
INSERT INTO articulos (codigo, nombre, descripcion, categoria, precio_compra, precio_venta, stock_actual, stock_minimo, proveedor) VALUES
                                                                                                                                       ('MART-001', 'Martillo de Carpintero', 'Martillo con mango de madera de 16oz', 'Herramientas Manuales', 8.50, 15.00, 25, 5, 'Distribuidora Hernández'),
                                                                                                                                       ('TORN-001', 'Tornillos 2" x 100 unidades', 'Caja de tornillos galvanizados 2 pulgadas', 'Ferretería', 3.25, 6.50, 150, 20, 'Suministros Industriales S.A.'),
                                                                                                                                       ('TALA-001', 'Taladro Eléctrico 500W', 'Taladro percutor con velocidad variable', 'Herramientas Eléctricas', 45.00, 89.99, 12, 3, 'Importadora TecnoTools'),
                                                                                                                                       ('PINTU-001', 'Pintura Látex Blanco 1Gal', 'Pintura látex lavable color blanco', 'Pinturas', 12.00, 22.50, 40, 10, 'Pinturas del Ecuador'),
                                                                                                                                       ('LIJA-001', 'Lijas Grano 120 x 10 hojas', 'Papel lija grano 120 para madera', 'Abrasivos', 2.00, 4.50, 200, 30, 'Distribuidora Hernández'),
                                                                                                                                       ('CERRADU-001', 'Cerradura de Pomo', 'Cerradura estándar con llave', 'Cerrajería', 15.00, 28.00, 18, 5, 'Seguridad Total'),
                                                                                                                                       ('CABLE-001', 'Cable Eléctrico #12 AWG x 100m', 'Cable de cobre calibre 12', 'Electricidad', 65.00, 110.00, 8, 2, 'Electro Suministros'),
                                                                                                                                       ('CANDADO-001', 'Candado de Alta Seguridad 50mm', 'Candado con arco endurecido', 'Cerrajería', 10.00, 18.50, 30, 8, 'Seguridad Total'),
                                                                                                                                       ('BROCHA-001', 'Brocha 3 pulgadas', 'Brocha para pintura cerda sintética', 'Pinturas', 3.50, 7.00, 45, 10, 'Pinturas del Ecuador'),
                                                                                                                                       ('PEGA-001', 'Pegamento PVC 100ml', 'Adhesivo para tuberías PVC', 'Plomería', 2.75, 5.50, 80, 15, 'Suministros Industriales S.A.');

-- Crear vista para artículos con stock bajo
CREATE OR REPLACE VIEW articulos_stock_bajo AS
SELECT
    codigo,
    nombre,
    categoria,
    stock_actual,
    stock_minimo,
    (stock_minimo - stock_actual) as faltante,
    proveedor
FROM articulos
WHERE stock_actual < stock_minimo AND activo = TRUE;

-- Crear vista para resumen de inventario por categoría
CREATE OR REPLACE VIEW resumen_inventario AS
SELECT
    categoria,
    COUNT(*) as total_articulos,
    SUM(stock_actual) as stock_total,
    SUM(stock_actual * precio_compra) as valor_compra,
    SUM(stock_actual * precio_venta) as valor_venta,
    SUM(stock_actual * (precio_venta - precio_compra)) as utilidad_potencial
FROM articulos
WHERE activo = TRUE
GROUP BY categoria;

-- Mostrar información de las tablas creadas
SHOW TABLES;
SELECT COUNT(*) as total_articulos FROM articulos;
SELECT * FROM articulos_stock_bajo;
SELECT * FROM resumen_inventario;