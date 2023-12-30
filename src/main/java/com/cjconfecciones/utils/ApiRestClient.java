package com.cjconfecciones.utils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.NoArgsConstructor;

@Named
@RequestScoped
@NoArgsConstructor
public class ApiRestClient implements Serializable{
	
	@Inject
	private Propiedades propiedades;
	
	Logger log = Logger.getLogger(ApiRestClient.class.getName());
	
	public <T> T consumeWebServices(Class<T> classResponse, String resource, String json) {
		//String url = "http://localhost:8080/back/rest/order/new";
		log.info(json);
		String url = propiedades.getOrderProperties("apiCore");
		url = url.concat(resource);
		T response = null;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(json));
			HttpResponse responsePeticion =  httpClient.execute(httpPost);
			if(responsePeticion.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = responsePeticion.getEntity();
				String jsonResponse = entity !=null ? EntityUtils.toString(entity):"";
				Type tipoRespuesta =  TypeToken.getParameterized(classResponse).getType();
				response = new Gson().fromJson(jsonResponse	,tipoRespuesta);
			}else {
				log.info("ERROR CODE HTTP");
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO CONSUME WS ",e);
		}
		return response;
	}
    


}
