package com.ferreteria.inventario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.ferreteria.inventario.entity")
@EnableJpaRepositories("com.ferreteria.inventario.repository")
public class FerreteriaInventarioApplication {

	private static final Logger logger = LoggerFactory.getLogger(FerreteriaInventarioApplication.class);

	public static void main(String[] args) {
		logger.info("==========================================================");
		logger.info("Iniciando Sistema de Inventario para Ferretería");
		logger.info("Arquitectura N-Capas con Servicios SOAP");
		logger.info("==========================================================");

		SpringApplication.run(FerreteriaInventarioApplication.class, args);

		logger.info("==========================================================");
		logger.info("✅ Aplicación iniciada exitosamente");
		logger.info("📱 Interfaz Web: http://localhost:8086/");
		logger.info("🌐 Servicio SOAP: http://localhost:8086/soap/ArticuloService");
		logger.info("📄 WSDL: http://localhost:8086/soap/ArticuloService?wsdl");
		logger.info("==========================================================");
	}
}
