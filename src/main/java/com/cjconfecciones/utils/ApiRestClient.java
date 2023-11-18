package com.cjconfecciones.utils;

import java.io.Closeable;
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
import jakarta.inject.Named;

@Named
@RequestScoped
public class ApiRestClient {
	
	Logger log = Logger.getLogger(ApiRestClient.class.getName());
	
	public <T> T consumeWebServices(Class<T> classResponse) {
		String url = "http://localhost:8080/back/rest/order/new";
		String jsonBody = "{\n"
				+ "        \"persona\":{\n"
				+ "                \"cedula\":\"0104809499\",\n"
				+ "                \"nombre\":\"JUAN\",\n"
				+ "                \"apellido\":\"PEREZ\",\n"
				+ "                \"telefono\":\"0998348972\",\n"
				+ "                \"direccion\":\"New York\"\n"
				+ "        },\n"
				+ "        \"cabecera\":{\n"
				+ "                \"fecha\":\"31-12-2023 16:51:00\",\n"
				+ "                \"total\":51,\n"
				+ "                \"estado\":\"A\"\n"
				+ "                },\n"
				+ "        \"detalles\":[\n"
				+ "                        {\n"
				+ "                                \"fecha\":\"31-12-2023 16:51:00\",\n"
				+ "                                \"unidades\":50,\n"
				+ "                                \"descripcion\":\"BORDADOS POR MAQUINA\",\n"
				+ "                                \"vunitario\":10,\n"
				+ "                                \"total\":11\n"
				+ "                        },{\n"
				+ "                                \"fecha\":\"31-12-2023 16:51:00\",\n"
				+ "                                \"unidades\":60,\n"
				+ "                                \"descripcion\":\"BORDADOS POR MAQUINAi2\",\n"
				+ "                                \"vunitario\":20,\n"
				+ "                                \"total\":21\n"
				+ "                        }\n"
				+ "                ]\n"
				+ "}";
		T response = null;
		try (CloseableHttpClient httpClient = HttpClients.createDefault()){
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(jsonBody));
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
