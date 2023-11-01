package com.cjconfecciones.controller;

import java.io.Serializable;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.Calculator;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named
@SessionScoped
public class SessionController implements Serializable{

	private Calculator calculator = new Calculator();
	
	Logger log = Logger.getLogger(SessionController.class.getName());
	
	@PostConstruct
	public void init() {
		log.info("SE INICIO EL BEAN DE SESSION");
	}

	public Calculator getCalculator() {
		return calculator;
	}

	public void setCalculator(Calculator calculator) {
		this.calculator = calculator;
	}

	
}
