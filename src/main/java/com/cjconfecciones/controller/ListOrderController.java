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
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
	private BigDecimal saldo;
	private  BigDecimal totalAbonos;
	private Date finicio;
	private Date ffin;

	private String msgAlert;

	@PostConstruct
	public void init() {
		try {
			log.info("POST CONSTRUCTOR LISTORDERCONTROLLER");
			lstAbonoSelects = new ListAbono();
			abonoSelected = new Abono();
			orderSelected = new Order();
			lstAbonoSelects.setAbonos(new ArrayList<>());
			searchOrdersInit();
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN INICIALIZATE ORDER ",e);
		}
	}

	private void searchOrdersInit() {
		try {
			ffin = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(ffin);
			calendar.add(Calendar.MONTH, -1);
			finicio = calendar.getTime();
			filtrar();
			/**
			finicio = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(finicio);
			calendar.add(Calendar.MONTH, -1);
			ffin = calendar.getTime();
			filtrar();
			 */
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO SEARCH ORDERS ",e);
		}
	}

	public void saveAbono(){
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			BigDecimal totalAbonos = lstAbonoSelects.getAbonos().stream().map(Abono::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal subTotal = totalAbonos.add(abonoSelected.getValor());
			if(subTotal.compareTo(orderSelected.getTotal())>0){
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
		    externalContext.redirect("bill.jsf");
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
					totalAbonos = lstAbonoSelects.getAbonos().stream().map(Abono::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
					saldo =  orderSelected.getTotal().subtract(totalAbonos);
				}else{
					saldo =  BigDecimal.ZERO;
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
			this.totalAbonos = BigDecimal.ZERO;
			this.saldo = BigDecimal.ZERO;
			this.abonoSelected = new Abono();
			orderSelected = this.orders.getPedidos().stream().filter(pedido -> Integer.parseInt(orderId)==(pedido.getId())).findFirst().orElse(null);
			this.listarAbono(orderId);
			this.updateEstadoPago();
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO MOPDIFY ORDER ",e);
		}
	}

	public void deleteOrder(String order){
		FacesContext context = FacesContext.getCurrentInstance();
		try{
			log.info("click check");
			Bill billAux = new Bill();
			billAux.setPedidoId(order);
			ResponseCJ response = apiRestClient.consumeWebServices(ResponseCJ.class, "order/changeStatus",util.converterJson(billAux));
			log.info(response.getError());
			if(response.getError().equals(EnumCJ.OK.getEstado())){
				orders.getPedidos().clear();//jj
				orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders", "");
				PrimeFaces.current().ajax().update("billForm:ordersIdTable");
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "PEDIDO ELIMINADO CORRECTAMENTE"));
			}
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO MOPDIFY ORDER ",e);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "INTENTE NUEVAMENTE"));
		}
	}

	public void closeWindowsAbono(){
		try{
//			orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders", "");
//			PrimeFaces.current().ajax().update("billForm:ordersIdTable");
			this.filtrar();
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO MOPDIFY ORDER ",e);
		}
	}

	public void filtrar(){
		try{
			if(finicio==null||ffin==null){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "TODOS LOS CAMPOS SON NECESARIOS"));
				return;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add("finicial", sdf.format(finicio));
			jsonBuilder.add("ffinal", sdf.format(ffin));
			orders = apiRestClient.consumeWebServices(ListOrder.class, "order/getOrders4date", jsonBuilder.build().toString());
			if(orders.getPedidos().isEmpty()){
				msgAlert="NO SE HAN ENCONTRADO PEDIDOS PARA ESE RANGO DE FECHAS";
				PrimeFaces.current().ajax().update("notificationId");
				PrimeFaces.current().executeScript("PF('notificacionDlg').show()");
			}
			log.info(orders.getPedidos().size()+"");
			PrimeFaces.current().ajax().update("billForm:ordersIdTable");
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO FILTER ",e);
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

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public BigDecimal getTotalAbonos() {
		return totalAbonos;
	}

	public void setTotalAbonos(BigDecimal totalAbonos) {
		this.totalAbonos = totalAbonos;
	}

	public Order getOrderSelected() {
		return orderSelected;
	}

	public void setOrderSelected(Order orderSelected) {
		this.orderSelected = orderSelected;
	}

	public Date getFinicio() {
		return finicio;
	}

	public void setFinicio(Date finicio) {
		this.finicio = finicio;
	}

	public Date getFfin() {
		return ffin;
	}

	public void setFfin(Date ffin) {
		this.ffin = ffin;
	}

	public String getMsgAlert() {
		return msgAlert;
	}

	public void setMsgAlert(String msgAlert) {
		this.msgAlert = msgAlert;
	}
}
