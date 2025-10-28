package com.ferreteria.inventario.config;

import com.ferreteria.inventario.soap.ArticuloSoapService;
import com.ferreteria.inventario.soap.ArticuloSoapServiceImpl;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Apache CXF para servicios SOAP (RNF5)
 * Cumple con estándares WSDL 1.1 y XML Schema
 *
 * @author Sistema Ferretería
 * @version 1.0
 */
@Configuration
public class CxfConfig {

    /**
     * Configura el servlet de CXF
     */
    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        ServletRegistrationBean<CXFServlet> servletRegistration =
                new ServletRegistrationBean<>(new CXFServlet(), "/soap/*");
        servletRegistration.setLoadOnStartup(1);
        servletRegistration.setName("CXFServlet");
        return servletRegistration;
    }

    /**
     * Configura el bus de CXF
     */
    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    /**
     * Publica el endpoint del servicio SOAP de artículos
     * Disponible en: http://localhost:8080/ferreteria/soap/ArticuloService
     * WSDL en: http://localhost:8080/ferreteria/soap/ArticuloService?wsdl
     */
    @Bean
    public Endpoint articuloSoapEndpoint(
            Bus bus,
            ArticuloSoapServiceImpl articuloSoapService) {

        EndpointImpl endpoint = new EndpointImpl(bus, articuloSoapService);
        endpoint.publish("/ArticuloService");

        return endpoint;
    }
}