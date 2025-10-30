package com.ferreteria.inventario.cliente.soap;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Cliente SOAP para consumir los servicios web de gestión de artículos
 * Permite insertar y consultar artículos mediante SOAP (RF5, RF6, RNF10)
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
public class ClienteSOAP {

    private static final String SOAP_ENDPOINT = "http://localhost:8080/ferreteria/soap/ArticuloService";
    private static final String NAMESPACE_URI = "http://soap.inventario.ferreteria.com/";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║    CLIENTE SOAP - SISTEMA DE INVENTARIO FERRETERÍA       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

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
                case 5:
                    System.out.println("\n✓ ¡Hasta luego!");
                    continuar = false;
                    break;
                default:
                    System.out.println("\n✗ Opción inválida. Intente nuevamente.");
            }

            if (continuar) {
                System.out.println("\nPresione Enter para continuar...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    /**
     * Muestra el menú principal
     */
    private static void mostrarMenu() {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("                    MENÚ PRINCIPAL");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("1. 📝 Insertar nuevo artículo");
        System.out.println("2. 🔍 Consultar artículo por código");
        System.out.println("3. ✏️  Actualizar artículo");
        System.out.println("4. 📦 Verificar stock disponible");
        System.out.println("5. 🚪 Salir");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.print("Seleccione una opción: ");
    }

    /**
     * Lee la opción del menú
     */
    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Inserta un nuevo artículo mediante SOAP (RF5)
     */
    private static void insertarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              INSERTAR NUEVO ARTÍCULO                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            // Solicitar datos del artículo
            System.out.print("\nCódigo (ej: MART-001): ");
            String codigo = scanner.nextLine().toUpperCase();

            System.out.print("Nombre: ");
            String nombre = scanner.nextLine();

            System.out.print("Descripción: ");
            String descripcion = scanner.nextLine();

            System.out.print("Categoría: ");
            String categoria = scanner.nextLine();

            System.out.print("Precio de Compra: ");
            BigDecimal precioCompra = new BigDecimal(scanner.nextLine());

            System.out.print("Precio de Venta: ");
            BigDecimal precioVenta = new BigDecimal(scanner.nextLine());

            System.out.print("Stock Actual: ");
            int stockActual = Integer.parseInt(scanner.nextLine());

            System.out.print("Stock Mínimo: ");
            int stockMinimo = Integer.parseInt(scanner.nextLine());

            System.out.print("Proveedor: ");
            String proveedor = scanner.nextLine();

            // Crear mensaje SOAP
            SOAPMessage soapMessage = crearMensajeInsertarArticulo(
                    codigo, nombre, descripcion, categoria,
                    precioCompra, precioVenta, stockActual, stockMinimo, proveedor
            );

            // Enviar solicitud
            System.out.println("\n⏳ Enviando solicitud al servidor SOAP...");
            SOAPMessage respuesta = enviarMensajeSOAP(soapMessage);

            // Procesar respuesta
            if (respuesta != null) {
                System.out.println("\n✓ RESPUESTA DEL SERVIDOR:");
                mostrarRespuestaSOAP(respuesta);
            }

        } catch (Exception e) {
            System.err.println("\n✗ Error al insertar artículo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Consulta un artículo por código mediante SOAP (RF6)
     */
    private static void consultarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              CONSULTAR ARTÍCULO                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\nIngrese el código del artículo: ");
            String codigo = scanner.nextLine().toUpperCase();

            // Crear mensaje SOAP
            SOAPMessage soapMessage = crearMensajeConsultarArticulo(codigo);

            // Enviar solicitud
            System.out.println("\n⏳ Consultando artículo...");
            SOAPMessage respuesta = enviarMensajeSOAP(soapMessage);

            // Procesar respuesta
            if (respuesta != null) {
                System.out.println("\n✓ INFORMACIÓN DEL ARTÍCULO:");
                mostrarRespuestaSOAP(respuesta);
            }

        } catch (Exception e) {
            System.err.println("\n✗ Error al consultar artículo: " + e.getMessage());
        }
    }

    /**
     * Actualiza un artículo existente
     */
    private static void actualizarArticulo() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              ACTUALIZAR ARTÍCULO                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\nIngrese el código del artículo a actualizar: ");
            String codigo = scanner.nextLine().toUpperCase();

            System.out.print("Nuevo nombre: ");
            String nombre = scanner.nextLine();

            System.out.print("Nueva descripción: ");
            String descripcion = scanner.nextLine();

            System.out.print("Nueva categoría: ");
            String categoria = scanner.nextLine();

            System.out.print("Nuevo precio de compra: ");
            BigDecimal precioCompra = new BigDecimal(scanner.nextLine());

            System.out.print("Nuevo precio de venta: ");
            BigDecimal precioVenta = new BigDecimal(scanner.nextLine());

            System.out.print("Nuevo stock actual: ");
            int stockActual = Integer.parseInt(scanner.nextLine());

            System.out.print("Nuevo stock mínimo: ");
            int stockMinimo = Integer.parseInt(scanner.nextLine());

            System.out.print("Nuevo proveedor: ");
            String proveedor = scanner.nextLine();

            // Crear mensaje SOAP
            SOAPMessage soapMessage = crearMensajeActualizarArticulo(
                    codigo, nombre, descripcion, categoria,
                    precioCompra, precioVenta, stockActual, stockMinimo, proveedor
            );

            // Enviar solicitud
            System.out.println("\n⏳ Actualizando artículo...");
            SOAPMessage respuesta = enviarMensajeSOAP(soapMessage);

            // Procesar respuesta
            if (respuesta != null) {
                System.out.println("\n✓ ARTÍCULO ACTUALIZADO:");
                mostrarRespuestaSOAP(respuesta);
            }

        } catch (Exception e) {
            System.err.println("\n✗ Error al actualizar artículo: " + e.getMessage());
        }
    }

    /**
     * Verifica si hay stock disponible
     */
    private static void verificarStock() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║              VERIFICAR STOCK DISPONIBLE                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        try {
            System.out.print("\nIngrese el código del artículo: ");
            String codigo = scanner.nextLine().toUpperCase();

            // Crear mensaje SOAP
            SOAPMessage soapMessage = crearMensajeVerificarStock(codigo);

            // Enviar solicitud
            System.out.println("\n⏳ Verificando stock...");
            SOAPMessage respuesta = enviarMensajeSOAP(soapMessage);

            // Procesar respuesta
            if (respuesta != null) {
                System.out.println("\n✓ RESULTADO:");
                mostrarRespuestaSOAP(respuesta);
            }

        } catch (Exception e) {
            System.err.println("\n✗ Error al verificar stock: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS DE CREACIÓN DE MENSAJES SOAP ====================

    /**
     * Crea mensaje SOAP para insertar artículo
     */
    private static SOAPMessage crearMensajeInsertarArticulo(
            String codigo, String nombre, String descripcion, String categoria,
            BigDecimal precioCompra, BigDecimal precioVenta,
            int stockActual, int stockMinimo, String proveedor) throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns", NAMESPACE_URI);

        SOAPBody soapBody = envelope.getBody();
        SOAPElement operacion = soapBody.addChildElement("insertarArticulo", "ns");

        SOAPElement request = operacion.addChildElement("articuloRequest", "ns");
        request.addChildElement("codigo", "ns").addTextNode(codigo);
        request.addChildElement("nombre", "ns").addTextNode(nombre);
        request.addChildElement("descripcion", "ns").addTextNode(descripcion);
        request.addChildElement("categoria", "ns").addTextNode(categoria);
        request.addChildElement("precioCompra", "ns").addTextNode(precioCompra.toString());
        request.addChildElement("precioVenta", "ns").addTextNode(precioVenta.toString());
        request.addChildElement("stockActual", "ns").addTextNode(String.valueOf(stockActual));
        request.addChildElement("stockMinimo", "ns").addTextNode(String.valueOf(stockMinimo));
        request.addChildElement("proveedor", "ns").addTextNode(proveedor);

        soapMessage.saveChanges();
        return soapMessage;
    }

    /**
     * Crea mensaje SOAP para consultar artículo
     */
    private static SOAPMessage crearMensajeConsultarArticulo(String codigo) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns", NAMESPACE_URI);

        SOAPBody soapBody = envelope.getBody();
        SOAPElement operacion = soapBody.addChildElement("consultarArticulo", "ns");
        operacion.addChildElement("codigo", "ns").addTextNode(codigo);

        soapMessage.saveChanges();
        return soapMessage;
    }

    /**
     * Crea mensaje SOAP para actualizar artículo
     */
    private static SOAPMessage crearMensajeActualizarArticulo(
            String codigo, String nombre, String descripcion, String categoria,
            BigDecimal precioCompra, BigDecimal precioVenta,
            int stockActual, int stockMinimo, String proveedor) throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns", NAMESPACE_URI);

        SOAPBody soapBody = envelope.getBody();
        SOAPElement operacion = soapBody.addChildElement("actualizarArticulo", "ns");
        operacion.addChildElement("codigo", "ns").addTextNode(codigo);

        SOAPElement request = operacion.addChildElement("articuloRequest", "ns");
        request.addChildElement("codigo", "ns").addTextNode(codigo);
        request.addChildElement("nombre", "ns").addTextNode(nombre);
        request.addChildElement("descripcion", "ns").addTextNode(descripcion);
        request.addChildElement("categoria", "ns").addTextNode(categoria);
        request.addChildElement("precioCompra", "ns").addTextNode(precioCompra.toString());
        request.addChildElement("precioVenta", "ns").addTextNode(precioVenta.toString());
        request.addChildElement("stockActual", "ns").addTextNode(String.valueOf(stockActual));
        request.addChildElement("stockMinimo", "ns").addTextNode(String.valueOf(stockMinimo));
        request.addChildElement("proveedor", "ns").addTextNode(proveedor);

        soapMessage.saveChanges();
        return soapMessage;
    }

    /**
     * Crea mensaje SOAP para verificar stock
     */
    private static SOAPMessage crearMensajeVerificarStock(String codigo) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("ns", NAMESPACE_URI);

        SOAPBody soapBody = envelope.getBody();
        SOAPElement operacion = soapBody.addChildElement("verificarStock", "ns");
        operacion.addChildElement("codigo", "ns").addTextNode(codigo);

        soapMessage.saveChanges();
        return soapMessage;
    }

    // ==================== MÉTODOS DE ENVÍO Y PROCESAMIENTO ====================

    /**
     * Envía un mensaje SOAP al servidor
     */
    private static SOAPMessage enviarMensajeSOAP(SOAPMessage soapMessage) {
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            SOAPMessage soapResponse = soapConnection.call(soapMessage, SOAP_ENDPOINT);

            soapConnection.close();
            return soapResponse;

        } catch (Exception e) {
            System.err.println("\n✗ Error al comunicarse con el servidor SOAP:");
            System.err.println("   " + e.getMessage());
            System.err.println("\n⚠️  Asegúrese de que el servidor esté ejecutándose en: " + SOAP_ENDPOINT);
            return null;
        }
    }

    /**
     * Muestra la respuesta SOAP formateada
     */
    private static void mostrarRespuestaSOAP(SOAPMessage soapResponse) {
        try {
            System.out.println("\n" + "─".repeat(60));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Source sourceContent = soapResponse.getSOAPPart().getContent();
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(sourceContent, result);

            System.out.println(writer.toString());
            System.out.println("─".repeat(60));

        } catch (Exception e) {
            System.err.println("Error al mostrar respuesta: " + e.getMessage());
        }
    }
}