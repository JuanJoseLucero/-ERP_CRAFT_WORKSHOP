package com.cjconfecciones.controller;

import com.cjconfecciones.pojo.*;
import com.cjconfecciones.utils.ApiRestClient;
import com.cjconfecciones.utils.EnumCJ;
import com.cjconfecciones.utils.Util;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private Order orderSelected;
	private String estadoPago;
	
	@PostConstruct
	public void init() {
		try {
			log.info("POST CONSTRUCTOR LISTORDERCONTROLLER");
			lstAbonoSelects = new ListAbono();
			abonoSelected = new Abono();
			orderSelected = new Order();
			lstAbonoSelects.setAbonos(new ArrayList<>());
			orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders", "");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN INICIALIZATE ORDER ",e);
		}
	}

	public void saveAbono(){
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			BigDecimal totalAbonos = lstAbonoSelects.getAbonos().stream().map(Abono::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
			totalAbonos = totalAbonos.add(abonoSelected.getValor());
			if(totalAbonos.compareTo(orderSelected.getTotal())>0){
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso", "El monto escede el valor total"));
				return;
			}
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			abonoSelected.setFecha(formatter.format(new Date()));
			abonoSelected.setCcabecera(orderSelected.getId());
			ResponseCJ responseCJ = apiRestClient.consumeWebServices(ResponseCJ.class, "abono/persistAbono" , util.converterJson(abonoSelected));
			log.info(responseCJ.getError());
			if(responseCJ.getError().equals(EnumCJ.OK.getEstado())){
				this.listarAbono(String.valueOf(orderSelected.getId()));
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso", "Abono almacenado"));
				this.updateEstadoPago();
			}else{
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso", "Error al almacenar el abono"));
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

	public void updateEstadoPago(){
		try{
			BigDecimal totalAbonos = lstAbonoSelects.getAbonos().stream().map(Abono::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
			if(totalAbonos.compareTo(orderSelected.getTotal())==0){
				this.estadoPago="TOTALMENTE CANCELADO";
			}else{
				this.estadoPago = "ABONADO";
			}
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR UPDATE ESTADO PAGO ",e);
		}
	}
	public void preAbono(String orderId) {
		try {
			orderSelected = this.orders.getPedidos().stream().filter(pedido -> Integer.parseInt(orderId)==(pedido.getId())).findFirst().orElse(null);
			this.listarAbono(orderId);
			this.updateEstadoPago();
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

	public String getEstadoPago() {
		return estadoPago;
	}

	public void setEstadoPago(String estadoPago) {
		this.estadoPago = estadoPago;
	}
}
