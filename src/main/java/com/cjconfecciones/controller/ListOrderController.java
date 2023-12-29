package com.cjconfecciones.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.ListOrder;
import com.cjconfecciones.pojo.Order;
import com.cjconfecciones.utils.ApiRestClient;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ListOrderController implements Serializable{
	
	@Inject
	private ApiRestClient apiRestClient;
	
	@Inject
	private SessionController sessionController;
	
	Logger log = Logger.getLogger(ListOrderController.class.getName());
	private  ListOrder orders;
	
	@PostConstruct
	public void init() {
		try {
			log.info("POST CONSTRUCTOR LISTORDERCONTROLLER");
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

	public ListOrder getOrders() {
		return orders;
	}

	public void setOrders(ListOrder orders) {
		this.orders = orders;
	}

}
