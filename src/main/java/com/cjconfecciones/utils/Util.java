package com.cjconfecciones.utils;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Util implements Serializable {
	
	private Logger log = Logger.getLogger(Util.class.getName());
	
	public String converterJson(Object object) {
		String response = "";
		try {
			Gson gson = new Gson();
	        response =  gson.toJson(object);
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN CONVERTER OBJECT ",e);
		}
		return response;
		
	}

}
