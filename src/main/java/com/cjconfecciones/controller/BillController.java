package com.cjconfecciones.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cjconfecciones.pojo.ResponseCJ;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;

import com.cjconfecciones.pojo.Bill;
import com.cjconfecciones.pojo.DetailBill;
import com.cjconfecciones.utils.ApiRestClient;
import com.cjconfecciones.utils.GenerateReport;
import com.cjconfecciones.utils.Util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class BillController implements Serializable{

	@Inject
	private SessionController sessionController;
	
	@Inject 
	private GenerateReport generateReport;
	
	@Inject
	private ApiRestClient apiRestClient;
	
	@Inject
	private Util util;
	
	private Boolean disabledSave;
	
	Logger log = Logger.getLogger(BillController.class.getName());
	private Bill bill = new Bill();
	private DetailBill detailSelected;
	private Boolean editMode = false;
	
	@PostConstruct	
	private void init() {
		try {
			log.info(getPreviousPageUrl());
			if(sessionController.getBillSesion().getPedidoId()!=null) {
				log.info("-----------".concat(sessionController.getBillSesion().getPedidoId()));
				Bill billSaved = apiRestClient.consumeWebServices(Bill.class, "order/getOrderById",util.converterJson(sessionController.getBillSesion()));
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				billSaved.setFechaDate(sdf.parse(billSaved.getFecha()));
				bill = billSaved;
			}else {
				this.bill.setTotal(BigDecimal.ZERO);
			}
			disabledSave = true;
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO INIT CONTROLLER ",e);
		}
	}
	
	public void automaticCalculation() {
		try {
			log.info("AUTOMATIC CALCULATION");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR AUTOMATIC CALCULATION ",e);
		}
	}
	
	 public String getPreviousPageUrl() {
	        FacesContext context = FacesContext.getCurrentInstance();
	        ExternalContext externalContext = context.getExternalContext();
	        
	        // Obtener el encabezado referer que contiene la URL de la página anterior
	        String previousPageUrl = externalContext.getRequestHeaderMap().get("referer");

	        return previousPageUrl;
	    }
	
	public void  searchClient() {
		log.info("objecto " .concat(util.converterJson(bill)));
		Bill responseWS =  apiRestClient.consumeWebServices(Bill.class, "order/searchClient",util.converterJson(bill));
		//log.info(responseWS.getNombres());
		//this.bill.setDireccion(responseWS.getDireccion());
		if(responseWS.getIdentificacion()!=null) {
			this.bill = responseWS;
			//this.bill.setTotal(BigDecimal.ZERO);
		}
	}
	
	
	public Boolean validatePreSave() {
		try {
			if (this.bill.getFechaDate()==null) {
				FacesContext context = FacesContext.getCurrentInstance();
	            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Fecha obligatoria"));
	            return false;
			}
			return true;
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN PRE-SAVED ",e);
			return false;
		}
	}
	
	public void persistWorkOrder() {
		try {
			if(validatePreSave()) {
				
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				bill.setFecha(formatter.format(bill.getFechaDate()));
				bill.setLstDetailBill(this.converterDateDetail(bill.getLstDetailBill()));
				String objecto = util.converterJson(bill);
				log.info("objecto " .concat(objecto));
				
				ResponseCJ responseWS =  apiRestClient.consumeWebServices(ResponseCJ.class, "order/new",util.converterJson(bill));
				if(responseWS.getError().equals("0")){
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "EXITOSO", "SE ALMACENO CORRECTAMENTE"));
					//this.generateBill();
				    ExternalContext externalContext = context.getExternalContext();
				    externalContext.redirect("listOrder.xhtml");
				}else{
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "INTENTE NUEVAMENTE"));
					log.info("ERROR");
				}
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN PERSIST BILL" ,e);
			FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "INTENTE NUEVAMENTE"));
		}
	}
	
	public List<DetailBill> converterDateDetail (List<DetailBill> detail){
		try {
			for (DetailBill o  :detail) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				o.setFechaCadena(sdf.format(o.getFecha()));
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO CONVERTER DETAIL DATE",e);
		}
		return detail;
	}
	
	public void newDetail() {
		this.editMode =false;
		detailSelected = new DetailBill();
		setDisabledSave(true);
		PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
		PrimeFaces.current().ajax().update("dialogs:btnCalcular");
	}
	
	public void editModeTrue() {
		this.editMode = true;
	}
	
	public void cleanDialog() {
		detailSelected = new DetailBill();
		setDisabledSave(true);
		PrimeFaces.current().ajax().update("dialogs:panel");
	}
	
	public void getCalculatedValues(){
        try{
            log.info("entro a get calculator valores");
            //if(validationsFields()){
            	if(detailSelected.getPuntadas()!=null&&detailSelected.getValorUnitario()!=null) {
            		this.detailSelected.setTotal((detailSelected.getPuntadas().multiply(detailSelected.getValorUnitario())).divide(new BigDecimal("1000")) );
                    setDisabledSave(false);
                    PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
            	}
                
                //this.detailSelected.setValorFinal((detailSelected.getPuntadas().multiply(detailSelected.getValorUnitario())).divide(new BigDecimal("1000")));
                //log.info(this.calculator.getTotal()+"");
            //}
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO CALCULATED VALUES ",e);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Intente nuevamente"));
        }
    }
	
	
	public void blurValorFinal() {
		try {
			if(detailSelected.getUnidades()!=null) {
				this.blurUnidades();
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO BLUR UNIDADES ",e);
		}
	}
	
	public void blurUnidades() {
		try {
			//detailSelected.setTotal(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
			detailSelected.setSubValorFactura(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
			setDisabledSave(false);
			PrimeFaces.current().ajax().update("dialogs:subTotal");
			PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO BLUR UNIDADES ",e);
		}
	}
	
	public void saveProduct() {
		try {
			if (!editMode) {
				if (this.validatePreSendBill()) {
					//detailSelected.setTotal(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
					detailSelected.setFecha(new Date());
					this.bill.getLstDetailBill().add(detailSelected);
					this.bill.setTotal(bill.getTotal().add(this.detailSelected.getSubValorFactura()));
					PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
					PrimeFaces.current().ajax().update("billForm:total");
				}else {
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Valor Final y Unidades obligatorios"));
				}
			}else {
				this.bill.setTotal(this.calculateTotalBill());
				PrimeFaces.current().ajax().update("billForm:total");
				PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
			}
			PrimeFaces.current().executeScript("PF('manageProductDialog').hide()");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN SAVE DETAIL ",e);
		}
	}
	
	
	private BigDecimal calculateTotalBill() {
		BigDecimal respuesta = BigDecimal.ZERO;
		try {
			for(DetailBill detalle : this.bill.getLstDetailBill()) {
				respuesta = respuesta.add(detalle.getSubValorFactura());
			}
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO CALCULATE TOTAL BILL ",e);
		}
		return respuesta;
	}
	
	
	private Boolean validatePreSendBill() {
		try {
			if(this.detailSelected.getValorFinal()==null || this.detailSelected.getUnidades()== null) {
				return false;
			}
			return true;
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR PREVALIDATE SENF BILL ",e);
			return false;
		}
	}
	
	public void calcular() {
		log.info("unidades "+bill.getLstDetailBill().get(0).getUnidades());
		log.info("valor unitario"+sessionController.getCalculator().getValorUnitario());
		sessionController.getCalculator().setValorFinal(bill.getLstDetailBill().get(0).getUnidades()
				.multiply(sessionController.getCalculator().getValorFinal()));
		bill.getLstDetailBill().get(0).setTotal(sessionController.getCalculator().getValorFinal());
		log.info(bill.getLstDetailBill().get(0).getTotal() + "");
		// PrimeFaces.current().ajax().update("billForm:detalleFacturaId");

		// bill.getLstDetailBill().clear();
	}
	
	
	public void generateBill() {
		try {
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"); // Customize the date format as needed
	        String dateString = dateFormat.format(bill.getFechaDate());
					
			Map<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("identification", bill.getIdentificacion());
			parametros.put("name", bill.getNombres());
			parametros.put("date", dateString);
			parametros.put("direction", bill.getDireccion());
			parametros.put("units", bill.getLstDetailBill().get(0).getUnidades()+"");
			parametros.put("description", bill.getLstDetailBill().get(0).getDescripcion());
			parametros.put("vunits", bill.getLstDetailBill().get(0).getValorUnitario()+"");
			parametros.put("total", bill.getLstDetailBill().get(0).getTotal()+"");
			String billName = "BillNewMethod.pdf";
			generateReport.generateReport(billName, "/home/jlucero/JaspersoftWorkspace/MyReports/BillPrintCJ.jasper", parametros);
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO GENERATE FROM BILLCONTROLLER REPORT ",e);
		}
	}
	
	public void onCellEdit(CellEditEvent event) {
		log.info("entra eliminar3");
		bill.getLstDetailBill().clear();
    }
	
	public void blurValorUnitario() {
		try {
			//this.detailSelected.setTotal(BigDecimal.ZERO);
			this.getCalculatedValues();
			this.detailSelected.setValorFinal(null);
			this.detailSelected.setSubValorFactura(null); 
			PrimeFaces.current().ajax().update("dialogs:totalId");
			PrimeFaces.current().ajax().update("dialogs:valorFinalId");
			PrimeFaces.current().ajax().update("dialogs:subTotal");
			setDisabledSave(true);
            PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
            PrimeFaces.current().ajax().update("dialogs:btnCalcular");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO BLUR ON PUNTADAS ",e);
		}
	}
	
	public void blurPuntadas() {
		try {
			if(this.detailSelected.getValorUnitario()!=null&&this.detailSelected.getTotal()!=null ) {
				blurValorUnitario();
				this.detailSelected.setValorFinal(null);
				this.detailSelected.setSubValorFactura(null);
				PrimeFaces.current().ajax().update("dialogs:valorFinalId");
				PrimeFaces.current().ajax().update("dialogs:subTotal");
				setDisabledSave(true);
				PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
			}else {
				this.detailSelected.setValorUnitario(null);
				this.detailSelected.setTotal(null);
				this.detailSelected.setValorFinal(null);
				PrimeFaces.current().ajax().update("dialogs:valorUnitarioId");
				PrimeFaces.current().ajax().update("dialogs:totalId");
				PrimeFaces.current().ajax().update("dialogs:valorFinalId");
				setDisabledSave(true);
				PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
				PrimeFaces.current().ajax().update("dialogs:btnCalcular");
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO BLUR ON PUNTADAS ", e);
		}
	}
	
	public void preDestroy() {
		this.sessionController.setBillSesion(new Bill());
	}
	
	/**
	 * <p:inputNumber id="valorUnitarioId" symbolPosition="s"
						decimalPlaces="2" decimalSeparator="." thousandSeparator=","
						value="#{billController.detailSelected.valorUnitario}" />

					<p:outputLabel for="@next" value="Total" />
					<p:inputNumber id="totalId" disabled="true" symbolPosition="s"
						decimalPlaces="2" decimalSeparator="." thousandSeparator=","
						value="#{billController.detailSelected.total}" />

					<p:outputLabel for="@next" value="Valor Final" />
					<p:inputNumber id="valorFinalId" symbolPosition="s"
						decimalPlaces="2" decimalSeparator="." thousandSeparator=","
						value="#{billController.detailSelected.valorFinal}" />
	 * @return
	 */


	public Bill getBill() {
		return bill;
	}


	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public DetailBill getDetailSelected() {
		return detailSelected;
	}

	public void setDetailSelected(DetailBill detailSelected) {
		this.detailSelected = detailSelected;
	}

	public Boolean getDisabledSave() {
		return disabledSave;
	}

	public void setDisabledSave(Boolean disabledSave) {
		this.disabledSave = disabledSave;
	}

}
