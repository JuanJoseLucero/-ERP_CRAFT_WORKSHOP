package com.cjconfecciones.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.*;
import com.cjconfecciones.utils.ApiRestClient;

import com.cjconfecciones.utils.EnumCJ;
import com.cjconfecciones.utils.Util;
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
	@Inject
	private Util util;
	
	Logger log = Logger.getLogger(ListOrderController.class.getName());
	private  ListOrder orders;
	private ListAbono lstAbonoSelects;
	private Abono abonoSelected;
	private Bill billSelected;
	
	@PostConstruct
	public void init() {
		try {
			log.info("POST CONSTRUCTOR LISTORDERCONTROLLER");
			lstAbonoSelects = new ListAbono();
			abonoSelected = new Abono();
			billSelected = new Bill();
			lstAbonoSelects.setAbonos(new ArrayList<>());
			orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders", "");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN INICIALIZATE ORDER ",e);
		}
	}

	public void saveAbono(){
		try{
			log.info("Se procede a almacenar un nuevo abono");
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			abonoSelected.setFecha(formatter.format(new Date()));
			abonoSelected.setCcabecera(Integer.parseInt(billSelected.getPedidoId()));
			ResponseCJ responseCJ = apiRestClient.consumeWebServices(ResponseCJ.class, "abono/persistAbono" , util.converterJson(abonoSelected));
			log.info(responseCJ.getError());
			if(responseCJ.getError().equals(EnumCJ.OK.getEstado())){
				this.listarAbono(billSelected.getPedidoId());
			}else{
				log.info("ERROR AL GUARDAR EL ABONO");
			}
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO SAVE ABONO ",e);
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

	public void listarAbono(String orderId){
		try{
			billSelected.setPedidoId(orderId);
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
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR EN EL METODO GENERICO PARA LISTAR LOS ABONOS ", e);
		}
	}

	public void preAbono(String orderId) {
		try {
			this.listarAbono(orderId);
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

	public Abono getAbonoSelected() {
		return abonoSelected;
	}

	public void setAbonoSelected(Abono abonoSelected) {
		this.abonoSelected = abonoSelected;
	}
}
