package com.ferreteria.inventario.cliente.soap;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Cliente SOAP SIMPLE - SIN necesidad de WSDL
 * Usa Apache CXF JaxWsProxyFactory para conectarse directamente al servicio
 *
 * VENTAJAS:
 * - No necesita parsear WSDL
 * - Código más limpio y simple
 * - Usa las interfaces Java directamente
 *
 * REQUISITO: Las clases del servidor deben estar accesibles
 * (ArticuloSoapService, ArticuloRequest, ArticuloResponse)
 *
 * @author Sistema Ferretería
 * @version 4.0 - Cliente Simplificado
 */
public class ClienteSOAP {

    private static final String SERVICE_URL = "http://localhost:8086/soap/ArticuloService";

    private static final Scanner scanner = new Scanner(System.in);

    // Proxy del servicio SOAP
    private static ArticuloSoapServiceProxy service;

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║    CLIENTE SOAP - SISTEMA DE INVENTARIO FERRETERÍA       ║");
        System.out.println("║              (Versión Simplificada)                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Inicializar conexión
        if (!conectarServicio()) {
            System.err.println(" No se pudo conectar al servicio. Saliendo...");
            return;
        }

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = leerOpcion();

            switch (opcion) {
                case 1:
                    insertarArticulo();
                    break;
                case 2:
                    consultarArticulo();
                    break;
                case 3:
                    actualizarArticulo();
                    break;
                case 4:
                    verificarStock();
                    break;
                case 0:
                    System.out.println("\n ¡Hasta luego!");
                    continuar = false;
                    break;
                default:
                    System.out.println("\n Opción inválida");
            }

            if (continuar) {
                System.out.println("\n📌 Presione Enter para continuar...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    /**
     * Conecta al servicio SOAP sin necesidad de WSDL
     */
    private static boolean conectarServicio() {
        try {
            System.out.println(" Conectando al servicio SOAP...");
            System.out.println(" URL: " + SERVICE_URL);

            service = new ArticuloSoapServiceProxy(SERVICE_URL);

            System.out.println(" Conexión establecida correctamente");
            System.out.println();
            return true;

        } catch (Exception e) {
            System.err.println(" Error al conectar: " + e.getMessage());
            System.err.println("\n  Verificaciones:");
            System.err.println("   1. ¿El servidor está ejecutándose?");
            System.err.println("   2. ¿El puerto 8086 está disponible?");
            System.err.println("   3. URL correcta: " + SERVICE_URL);
            return false;
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("                    MENÚ PRINCIPAL");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("1. 📝 Insertar nuevo artículo");
        System.out.println("2. 🔍 Consultar artículo por código");
        System.out.println("3. ✏️  Actualizar artículo");
        System.out.println("4. 📦 Verificar stock disponible");
        System.out.println("0. 🚪 Salir");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.print("➤ Seleccione una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 1. INSERTAR ARTÍCULO
     */
    private static void insertarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              INSERTAR NUEVO ARTÍCULO                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            ArticuloRequestDTO request = new ArticuloRequestDTO();

            System.out.print("\n Código (ej: MART-001): ");
            request.codigo = scanner.nextLine().toUpperCase().trim();

            System.out.print(" Nombre: ");
            request.nombre = scanner.nextLine().trim();

            System.out.print(" Descripción: ");
            request.descripcion = scanner.nextLine().trim();

            System.out.print(" Categoría: ");
            request.categoria = scanner.nextLine().trim();

            System.out.print(" Precio de Compra: $");
            request.precioCompra = new BigDecimal(scanner.nextLine().trim());

            System.out.print(" Precio de Venta: $");
            request.precioVenta = new BigDecimal(scanner.nextLine().trim());

            System.out.print(" Stock Actual: ");
            request.stockActual = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(" Stock Mínimo: ");
            request.stockMinimo = Integer.parseInt(scanner.nextLine().trim());

            System.out.print(" Proveedor: ");
            request.proveedor = scanner.nextLine().trim();

            System.out.println("\n Enviando solicitud...");
            ArticuloResponseDTO response = service.insertarArticulo(request);

            mostrarRespuesta(response, "ARTÍCULO INSERTADO");

        } catch (Exception e) {
            System.err.println("\n Error: " + e.getMessage());
        }
    }

    /**
     * 2. CONSULTAR ARTÍCULO
     */
    private static void consultarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              CONSULTAR ARTÍCULO                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\n Código del artículo: ");
            String codigo = scanner.nextLine().toUpperCase().trim();

            System.out.println("\n Consultando...");
            ArticuloResponseDTO response = service.consultarArticulo(codigo);

            mostrarRespuesta(response, "INFORMACIÓN DEL ARTÍCULO");

        } catch (Exception e) {
            System.err.println("\n Error: " + e.getMessage());
        }
    }

    /**
     * 3. ACTUALIZAR ARTÍCULO
     */
    private static void actualizarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              ACTUALIZAR ARTÍCULO                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\n🔍 Código del artículo: ");
            String codigo = scanner.nextLine().toUpperCase().trim();

            ArticuloRequestDTO request = new ArticuloRequestDTO();
            request.codigo = codigo;

            System.out.println("\n Nuevos datos:");

            System.out.print("Nombre: ");
            request.nombre = scanner.nextLine().trim();

            System.out.print("Descripción: ");
            request.descripcion = scanner.nextLine().trim();

            System.out.print("Categoría: ");
            request.categoria = scanner.nextLine().trim();

            System.out.print("Precio de Compra: $");
            request.precioCompra = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Precio de Venta: $");
            request.precioVenta = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Stock Actual: ");
            request.stockActual = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Stock Mínimo: ");
            request.stockMinimo = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Proveedor: ");
            request.proveedor = scanner.nextLine().trim();

            System.out.println("\n Actualizando...");
            ArticuloResponseDTO response = service.actualizarArticulo(codigo, request);

            mostrarRespuesta(response, "ARTÍCULO ACTUALIZADO");

        } catch (Exception e) {
            System.err.println("\n Error: " + e.getMessage());
        }
    }

    /**
     * 4. VERIFICAR STOCK
     */
    private static void verificarStock() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              VERIFICAR STOCK                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\n Código del artículo: ");
            String codigo = scanner.nextLine().toUpperCase().trim();

            System.out.println("\n Verificando...");
            boolean disponible = service.verificarStock(codigo);

            System.out.println("\n" + "═".repeat(60));
            System.out.println("  RESULTADO DE VERIFICACIÓN");
            System.out.println("═".repeat(60));

            if (disponible) {
                System.out.println(" Stock disponible: SÍ");
                System.out.println("   El artículo tiene stock suficiente");
            } else {
                System.out.println(" Stock disponible: NO");
                System.out.println("    El artículo tiene stock bajo o no disponible");
            }
            System.out.println("═".repeat(60));

        } catch (Exception e) {
            System.err.println("\n Error: " + e.getMessage());
        }
    }

    /**
     * Muestra la respuesta del servicio formateada
     */
    private static void mostrarRespuesta(ArticuloResponseDTO response, String titulo) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  " + titulo);
        System.out.println("═".repeat(60));

        if (response.mensaje != null && !response.mensaje.isEmpty()) {
            System.out.println("📌 " + response.mensaje);
            System.out.println("─".repeat(60));
        }

        System.out.printf("🆔 ID:                %d%n", response.id);
        System.out.printf("📝 Código:            %s%n", response.codigo);
        System.out.printf("📦 Nombre:            %s%n", response.nombre);
        System.out.printf("📄 Descripción:       %s%n", response.descripcion);
        System.out.printf("📁 Categoría:         %s%n", response.categoria);
        System.out.printf("💰 Precio Compra:     $%.2f%n", response.precioCompra);
        System.out.printf("💵 Precio Venta:      $%.2f%n", response.precioVenta);
        System.out.printf("📊 Stock Actual:      %d unidades%n", response.stockActual);
        System.out.printf("⚠️  Stock Mínimo:      %d unidades%n", response.stockMinimo);
        System.out.printf("🏢 Proveedor:         %s%n", response.proveedor);
        System.out.printf("📈 Margen Ganancia:   %.2f%%%n", response.margenGanancia);

        if (response.tieneStockBajo) {
            System.out.println("\n ALERTA: Stock bajo detectado");
        }

        System.out.println("═".repeat(60));
    }

    // ==================== CLASES DTO SIMPLIFICADAS ====================

    /**
     * Proxy simplificado del servicio SOAP
     */
    static class ArticuloSoapServiceProxy {
        private final com.ferreteria.inventario.soap.ArticuloSoapService service;

        public ArticuloSoapServiceProxy(String serviceUrl) {
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(com.ferreteria.inventario.soap.ArticuloSoapService.class);
            factory.setAddress(serviceUrl);
            this.service = (com.ferreteria.inventario.soap.ArticuloSoapService) factory.create();
        }

        public ArticuloResponseDTO insertarArticulo(ArticuloRequestDTO request) throws Exception {
            com.ferreteria.inventario.soap.dto.ArticuloRequest req = convertirRequest(request);
            com.ferreteria.inventario.soap.dto.ArticuloResponse resp = service.insertarArticulo(req);
            return convertirResponse(resp);
        }

        public ArticuloResponseDTO consultarArticulo(String codigo) throws Exception {
            com.ferreteria.inventario.soap.dto.ArticuloResponse resp = service.consultarArticulo(codigo);
            return convertirResponse(resp);
        }

        public ArticuloResponseDTO actualizarArticulo(String codigo, ArticuloRequestDTO request) throws Exception {
            com.ferreteria.inventario.soap.dto.ArticuloRequest req = convertirRequest(request);
            com.ferreteria.inventario.soap.dto.ArticuloResponse resp = service.actualizarArticulo(codigo, req);
            return convertirResponse(resp);
        }

        public boolean verificarStock(String codigo) throws Exception {
            return service.verificarStock(codigo);
        }

        private com.ferreteria.inventario.soap.dto.ArticuloRequest convertirRequest(ArticuloRequestDTO dto) {
            com.ferreteria.inventario.soap.dto.ArticuloRequest req =
                    new com.ferreteria.inventario.soap.dto.ArticuloRequest();
            req.setCodigo(dto.codigo);
            req.setNombre(dto.nombre);
            req.setDescripcion(dto.descripcion);
            req.setCategoria(dto.categoria);
            req.setPrecioCompra(dto.precioCompra);
            req.setPrecioVenta(dto.precioVenta);
            req.setStockActual(dto.stockActual);
            req.setStockMinimo(dto.stockMinimo);
            req.setProveedor(dto.proveedor);
            return req;
        }

        private ArticuloResponseDTO convertirResponse(com.ferreteria.inventario.soap.dto.ArticuloResponse resp) {
            ArticuloResponseDTO dto = new ArticuloResponseDTO();
            dto.id = resp.getId();
            dto.codigo = resp.getCodigo();
            dto.nombre = resp.getNombre();
            dto.descripcion = resp.getDescripcion();
            dto.categoria = resp.getCategoria();
            dto.precioCompra = resp.getPrecioCompra();
            dto.precioVenta = resp.getPrecioVenta();
            dto.stockActual = resp.getStockActual();
            dto.stockMinimo = resp.getStockMinimo();
            dto.proveedor = resp.getProveedor();
            dto.margenGanancia = resp.getMargenGanancia();
            dto.tieneStockBajo = resp.getTieneStockBajo();
            dto.mensaje = resp.getMensaje();
            return dto;
        }
    }

    /**
     * DTO para Request simplificado
     */
    static class ArticuloRequestDTO {
        String codigo;
        String nombre;
        String descripcion;
        String categoria;
        BigDecimal precioCompra;
        BigDecimal precioVenta;
        int stockActual;
        int stockMinimo;
        String proveedor;
    }

    /**
     * DTO para Response simplificado
     */
    static class ArticuloResponseDTO {
        Long id;
        String codigo;
        String nombre;
        String descripcion;
        String categoria;
        BigDecimal precioCompra;
        BigDecimal precioVenta;
        int stockActual;
        int stockMinimo;
        String proveedor;
        BigDecimal margenGanancia;
        boolean tieneStockBajo;
        String mensaje;
    }
}
