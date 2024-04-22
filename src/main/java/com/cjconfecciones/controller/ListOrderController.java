package com.cjconfecciones.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.Abono;
import com.cjconfecciones.pojo.ListAbono;
import com.cjconfecciones.pojo.ListOrder;
import com.cjconfecciones.pojo.Order;
import com.cjconfecciones.utils.ApiRestClient;

import com.cjconfecciones.utils.EnumCJ;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

@Named
@ViewScoped
public class ListOrderController implements Serializable{
	
	@Inject
	private ApiRestClient apiRestClient;
	
	@Inject
	private SessionController sessionController;
	
	Logger log = Logger.getLogger(ListOrderController.class.getName());
	private  ListOrder orders;
	private ListAbono lstAbonoSelects;
	
	@PostConstruct
	public void init() {
		try {
			log.info("POST CONSTRUCTOR LISTORDERCONTROLLER");
			lstAbonoSelects = new ListAbono();
			lstAbonoSelects.setAbonos(new ArrayList<>());
			orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders", "");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN INICIALIZATE ORDER ",e);
		}
	}
	
	public void modifyOrder(String orderId) {
		try {
			sessionController.getBillSesion().setPedidoId(orderId);
			FacesContext context = FacesContext.getCurrentInstance();
		    ExternalContext externalContext = context.getExternalContext();
		    externalContext.redirect("bill.xhtml");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO MOPDIFY ORDER ",e);
		}
	}

	public void preAbono(String orderId) {
		try {
			log.info("ABONO");
			JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			jsonObjectBuilder.add("pedidoId",Integer.parseInt(orderId));
			lstAbonoSelects = apiRestClient.consumeWebServices(ListAbono.class, "abono/getLst4cabecera", jsonObjectBuilder.build().toString());
			if(EnumCJ.OK.getEstado().equals(lstAbonoSelects.getError())){
				if(!lstAbonoSelects.getAbonos().isEmpty()){
					log.info(lstAbonoSelects.getAbonos().size()+"");
				}
			}else{
				log.info("ERROR AL CONSULTA EL ABONO");
			}
			log.info(lstAbonoSelects.getAbonos().size()+"");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO MOPDIFY ORDER ",e);
		}
	}

	public ListOrder getOrders() {
		return orders;
	}

	public void setOrders(ListOrder orders) {
		this.orders = orders;
	}

	public ListAbono getLstAbonoSelects() {
		return lstAbonoSelects;
	}

	public void setLstAbonoSelects(ListAbono lstAbonoSelects) {
		this.lstAbonoSelects = lstAbonoSelects;
	}
}
