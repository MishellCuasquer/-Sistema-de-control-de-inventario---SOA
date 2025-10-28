package com.ferreteria.inventario.controller;

import com.ferreteria.inventario.entity.Articulo;
import com.ferreteria.inventario.exception.ArticuloDuplicadoException;
import com.ferreteria.inventario.exception.ArticuloNoEncontradoException;
import com.ferreteria.inventario.exception.ValidationException;
import com.ferreteria.inventario.service.ArticuloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador web para la interfaz de usuario
 * IMPORTANTE: Sin @RequestMapping a nivel de clase para evitar conflictos
 */
@Controller
public class ArticuloController {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloController.class);

    @Autowired
    private ArticuloService articuloService;

    /** ============ PÁGINA PRINCIPAL ============ */
    @GetMapping({"/", "", "/index"})
    public String index(Model model) {
        logger.info("========== CARGANDO PÁGINA PRINCIPAL ==========");
        try {
            List<Articulo> articulos = articuloService.obtenerTodosLosArticulos();
            List<Articulo> articulosStockBajo = articuloService.obtenerArticulosConStockBajo();

            model.addAttribute("articulos", articulos);
            model.addAttribute("articulosStockBajo", articulosStockBajo);
            model.addAttribute("totalArticulos", articulos.size());
            model.addAttribute("articulosConStockBajo", articulosStockBajo.size());

            try {
                model.addAttribute("categorias", articuloService.obtenerTodasLasCategorias());
            } catch (Exception e) {
                model.addAttribute("categorias", new ArrayList<String>());
            }

            logger.info("✅ Página principal cargada: {} artículos", articulos.size());
            return "index";
        } catch (Exception e) {
            logger.error("❌ ERROR al cargar página principal", e);
            model.addAttribute("error", "Error al cargar los artículos: " + e.getMessage());
            return "error";
        }
    }

    /** ============ REGISTRAR - GET ============ */
    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        logger.info("========== MOSTRANDO FORMULARIO REGISTRAR ==========");
        try {
            model.addAttribute("articulo", new Articulo());

            try {
                model.addAttribute("categorias", articuloService.obtenerTodasLasCategorias());
                model.addAttribute("proveedores", articuloService.obtenerTodosLosProveedores());
            } catch (Exception e) {
                logger.warn("⚠️ Error al obtener categorías/proveedores, usando listas vacías");
                model.addAttribute("categorias", new ArrayList<String>());
                model.addAttribute("proveedores", new ArrayList<String>());
            }

            logger.info("✅ Formulario de registro cargado correctamente");
            return "registrar";
        } catch (Exception e) {
            logger.error("❌ ERROR al mostrar formulario registrar", e);
            model.addAttribute("error", "Error al cargar formulario: " + e.getMessage());
            return "error";
        }
    }

    /** ============ REGISTRAR - POST ============ */
    @PostMapping("/registrar")
    public String registrarArticulo(@ModelAttribute Articulo articulo,
                                    RedirectAttributes redirectAttributes) {
        logger.info("========== REGISTRANDO ARTÍCULO: {} ==========", articulo.getNombre());
        try {
            Articulo articuloGuardado = articuloService.insertarArticulo(articulo);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Artículo registrado exitosamente: " + articuloGuardado.getNombre());

            if (articuloGuardado.tieneStockBajo()) {
                redirectAttributes.addFlashAttribute("warning",
                        "⚠️ ALERTA: El artículo tiene stock bajo (" +
                                articuloGuardado.getStockActual() + " unidades)");
            }

            logger.info("✅ Artículo registrado con ID: {}", articuloGuardado.getId());
            return "redirect:/";

        } catch (ArticuloDuplicadoException e) {
            logger.warn("⚠️ Artículo duplicado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/registrar";
        } catch (ValidationException e) {
            logger.warn("⚠️ Errores de validación: {}", e.getErrores());
            redirectAttributes.addFlashAttribute("error",
                    "❌ Errores de validación: " + String.join(", ", e.getErrores()));
            return "redirect:/registrar";
        } catch (Exception e) {
            logger.error("❌ ERROR al registrar artículo", e);
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al registrar el artículo: " + e.getMessage());
            return "redirect:/registrar";
        }
    }

    /** ============ CONSULTAR - GET ============ */
    @GetMapping("/consultar")
    public String mostrarFormularioConsulta(Model model) {
        logger.info("========== MOSTRANDO FORMULARIO CONSULTAR ==========");
        try {
            model.addAttribute("categorias", articuloService.obtenerTodasLasCategorias());
        } catch (Exception e) {
            model.addAttribute("categorias", new ArrayList<String>());
        }
        logger.info("✅ Formulario de consulta cargado");
        return "consultar";
    }

    /** ============ BUSCAR ============ */
    @GetMapping("/buscar")
    public String buscarArticulos(@RequestParam(required = false) String criterio,
                                  @RequestParam(required = false) String categoria,
                                  Model model) {
        logger.info("========== BUSCANDO - Criterio: {}, Categoría: {} ==========",
                criterio, categoria);
        try {
            List<Articulo> resultados;

            if (categoria != null && !categoria.isEmpty()) {
                resultados = articuloService.obtenerArticulosPorCategoria(categoria);
            } else if (criterio != null && !criterio.isEmpty()) {
                resultados = articuloService.buscarArticulos(criterio);
            } else {
                resultados = articuloService.obtenerTodosLosArticulos();
            }

            model.addAttribute("articulos", resultados);
            model.addAttribute("criterio", criterio);
            model.addAttribute("categoria", categoria);
            model.addAttribute("totalResultados", resultados.size());

            try {
                model.addAttribute("categorias", articuloService.obtenerTodasLasCategorias());
            } catch (Exception e) {
                model.addAttribute("categorias", new ArrayList<String>());
            }

            logger.info("✅ Búsqueda completada: {} resultados", resultados.size());
            return "consultar";

        } catch (Exception e) {
            logger.error("❌ ERROR al buscar artículos", e);
            model.addAttribute("error", "Error al buscar: " + e.getMessage());
            return "consultar";
        }
    }

    /** ============ DETALLE ============ */
    @GetMapping("/articulo/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        logger.info("========== VER DETALLE ARTÍCULO ID: {} ==========", id);
        try {
            Articulo articulo = articuloService.consultarArticuloPorId(id)
                    .orElseThrow(() -> new ArticuloNoEncontradoException("ID: " + id));

            model.addAttribute("articulo", articulo);

            if (articulo.tieneStockBajo()) {
                model.addAttribute("stockBajo", true);
            }

            logger.info("✅ Detalle cargado para artículo: {}", articulo.getNombre());
            return "detalle";

        } catch (ArticuloNoEncontradoException e) {
            logger.warn("⚠️ Artículo no encontrado: {}", id);
            return "redirect:/";
        }
    }

    /** ============ EDITAR - GET ============ */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        logger.info("========== EDITAR ARTÍCULO ID: {} ==========", id);
        try {
            Articulo articulo = articuloService.consultarArticuloPorId(id)
                    .orElseThrow(() -> new ArticuloNoEncontradoException("ID: " + id));

            model.addAttribute("articulo", articulo);

            try {
                model.addAttribute("categorias", articuloService.obtenerTodasLasCategorias());
                model.addAttribute("proveedores", articuloService.obtenerTodosLosProveedores());
            } catch (Exception e) {
                model.addAttribute("categorias", new ArrayList<String>());
                model.addAttribute("proveedores", new ArrayList<String>());
            }

            logger.info("✅ Formulario de edición cargado");
            return "editar";

        } catch (ArticuloNoEncontradoException e) {
            logger.warn("⚠️ Artículo no encontrado para editar: {}", id);
            return "redirect:/";
        }
    }

    /** ============ EDITAR - POST ============ */
    @PostMapping("/editar/{id}")
    public String actualizarArticulo(@PathVariable Long id,
                                     @ModelAttribute Articulo articulo,
                                     RedirectAttributes redirectAttributes) {
        logger.info("========== ACTUALIZANDO ARTÍCULO ID: {} ==========", id);
        try {
            articulo.setId(id);
            Articulo articuloActualizado = articuloService.actualizarArticulo(articulo);

            redirectAttributes.addFlashAttribute("success",
                    "✅ Artículo actualizado: " + articuloActualizado.getNombre());

            if (articuloActualizado.tieneStockBajo()) {
                redirectAttributes.addFlashAttribute("warning",
                        "⚠️ ALERTA: Stock bajo");
            }

            logger.info("✅ Artículo actualizado correctamente");
            return "redirect:/";

        } catch (ArticuloNoEncontradoException e) {
            logger.warn("⚠️ Artículo no encontrado: {}", id);
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/";
        } catch (ValidationException e) {
            logger.warn("⚠️ Errores de validación: {}", e.getErrores());
            redirectAttributes.addFlashAttribute("error",
                    "❌ Errores: " + String.join(", ", e.getErrores()));
            return "redirect:/editar/" + id;
        } catch (Exception e) {
            logger.error("❌ ERROR al actualizar", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar");
            return "redirect:/editar/" + id;
        }
    }

    /** ============ ELIMINAR ============ */
    @PostMapping("/eliminar/{id}")
    public String eliminarArticulo(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {
        logger.info("========== ELIMINANDO ARTÍCULO ID: {} ==========", id);
        try {
            articuloService.eliminarArticulo(id);
            redirectAttributes.addFlashAttribute("success", "✅ Artículo eliminado");
            logger.info("✅ Artículo eliminado correctamente");
            return "redirect:/";
        } catch (ArticuloNoEncontradoException e) {
            logger.warn("⚠️ Artículo no encontrado: {}", id);
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            logger.error("❌ ERROR al eliminar", e);
            redirectAttributes.addFlashAttribute("error", "❌ Error al eliminar");
            return "redirect:/";
        }
    }

    /** ============ STOCK BAJO ============ */
    @GetMapping("/stock-bajo")
    public String articulosStockBajo(Model model) {
        logger.info("========== CONSULTANDO STOCK BAJO ==========");
        try {
            List<Articulo> articulosStockBajo = articuloService.obtenerArticulosConStockBajo();
            model.addAttribute("articulos", articulosStockBajo);
            model.addAttribute("total", articulosStockBajo.size());
            logger.info("✅ Stock bajo: {} artículos", articulosStockBajo.size());
            return "stock-bajo";
        } catch (Exception e) {
            logger.error("❌ ERROR al consultar stock bajo", e);
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /** ============ SOAP INFO ============ */
    @GetMapping("/soap-info")
    public String soapInfo(Model model) {
        logger.info("========== MOSTRANDO INFO SOAP ==========");

        String wsdlUrl = "http://localhost:8086/soap/ArticuloService?wsdl";
        String soapEndpoint = "http://localhost:8086/soap/ArticuloService";

        model.addAttribute("wsdlUrl", wsdlUrl);
        model.addAttribute("soapEndpoint", soapEndpoint);

        logger.info("✅ Info SOAP cargada");
        return "soap-info";
    }

    /** ============ MANEJO DE ERRORES ============ */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("========== ERROR GLOBAL ==========", e);
        model.addAttribute("error", "Error del sistema: " + e.getMessage());
        return "error";
    }
}