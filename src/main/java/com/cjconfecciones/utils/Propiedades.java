package com.cjconfecciones.utils;

import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.NoArgsConstructor;

@Named
@ApplicationScoped
@NoArgsConstructor
public class Propiedades implements Serializable {

	private Properties properties;
	static Logger log = Logger.getLogger(Propiedades.class.getName());

	@PostConstruct
	public void init() {
		properties = new Properties();
	}

	public String getOrderProperties(String data) {
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("OrderProperties.properties"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "PROBLEMAS AL OBTENER CONSULTAS DE PROOVEDORES", e);
		}
		return properties.getProperty(data);
	}
	
}
