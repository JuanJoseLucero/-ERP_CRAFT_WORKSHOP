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
import java.util.stream.Collectors;

import com.cjconfecciones.pojo.Abono;
import com.cjconfecciones.pojo.ResponseCJ;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import org.primefaces.event.SelectEvent;


@Named
@ViewScoped
//@ServletSecurity(httpMethodConstraints = { @HttpMethodConstraint(value = "GET", rolesAllowed = { "Manager" }) })
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
	private Boolean disabledSaveEstampados;
	private Boolean disabledSaveConfeccion;
	
	Logger log = Logger.getLogger(BillController.class.getName());
	private Bill bill = new Bill();
	private DetailBill detailSelected;
	private DetailBill detailSelected_estampado;
	private DetailBill detailSelected_confecciones;
	private Boolean editMode = false;
	private String indexTab= "0";
	private String valueAutocomplete;
	private ResponseCJ responseWS;
	
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
			disabledSaveEstampados = true;
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO INIT CONTROLLER ",e);
		}
	}

	public List<String>  methodAutocomplete(String query){
		List<String> lstNames = new ArrayList<>();
		try{
			this.bill.setNombres(query);
			JsonObject responseAutocomplete = apiRestClient.consumeWebServices(JsonObject.class, "test/search4name", util.converterJson(bill));
			if(responseAutocomplete.getString("error").equals("0")){
				JsonArray listaNombres = responseAutocomplete.getJsonArray("nombres");
				log.info(listaNombres.size()+"");

				List<String> names = listaNombres.getValuesAs(JsonObject.class)
						.stream()
						.map(persona -> persona.getString("nombre"))
						.collect(Collectors.toList());
				return names;
			}else{
				return new ArrayList<>();
			}
		}catch (Exception e){
			log.log(Level.SEVERE, "error method autocomplete ",e);
		}
		return lstNames;
	}

	public void onItemSelect(SelectEvent<String> event) {
		String nombreSeleccionado = event.getObject();

		log.info("El usuario seleccionó: " + nombreSeleccionado);

		// Aquí llamas al método específico que necesites
		//miMetodoEspecial(nombreSeleccionado);
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
				
				responseWS =  apiRestClient.consumeWebServices(ResponseCJ.class, "order/new",util.converterJson(bill));
				if(responseWS.getError().equals("0")){
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "EXITOSO", "SE ALMACENO CORRECTAMENTE"));
					//this.generateBill();
				    /*ExternalContext externalContext = context.getExternalContext();
				    externalContext.redirect("listOrder.xhtml");
				    */

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
		detailSelected_estampado = new DetailBill();
		detailSelected_confecciones = new DetailBill();
		setDisabledSave(true);
		PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
		PrimeFaces.current().ajax().update("dialogs:btnCalcular");
	}

	public void deleteRow(int index){
		try{
			DetailBill detailAux =  bill.getLstDetailBill().get(index);
			this.bill.setTotal(bill.getTotal().subtract(detailAux.getSubValorFactura()));
			bill.getLstDetailBill().remove(index);
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR TO DELETE ROW ",e);
		}
	}
	public void editModeTrue(String options) {
		this.editMode = true;
		if("E".equals(options)){
			this.detailSelected_confecciones = new DetailBill();
			this.detailSelected = new DetailBill();
			this.indexTab = "0";
		}else if("C".equals(options)){
			this.detailSelected_estampado = new DetailBill();
			this.detailSelected = new DetailBill();
			this.indexTab = "1";
		}else if("B".equals(options)){
			this.detailSelected_confecciones = new DetailBill();
			this.detailSelected_estampado = new DetailBill();
			this.indexTab = "2";
		}
	}

	
	public void cleanDialog() {
		detailSelected = new DetailBill();
		detailSelected_confecciones = new DetailBill();
		detailSelected_estampado = new DetailBill();
		setDisabledSave(true);
		setDisabledSaveConfeccion(true);
		setDisabledSaveEstampados(true);
		PrimeFaces.current().ajax().update("dialogs:idTabView");
	}
	
	public void getCalculatedValues(){
        try{
            log.info("entro a get calculator valores");
            //if(validationsFields()){
            	if(detailSelected.getPuntadas()!=null&&detailSelected.getValorUnitario()!=null) {
            		this.detailSelected.setTotal((detailSelected.getPuntadas().multiply(detailSelected.getValorUnitario())).divide(new BigDecimal("1000")) );
                    setDisabledSave(false);
                    PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
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

	public void blurSimpleValorUnitario(String option){
		try{
			if ("C".equals(option)){
				detailSelected_confecciones.setSubValorFactura(detailSelected_confecciones.getValorFinal().multiply(detailSelected_confecciones.getUnidades()!=null?detailSelected_confecciones.getUnidades():BigDecimal.ZERO));
				PrimeFaces.current().ajax().update("dialogs:idTabView:subTotalSimpleConfeccion");
				if(detailSelected_confecciones.getSubValorFactura()!=null &&
						detailSelected_confecciones.getSubValorFactura().compareTo(BigDecimal.ZERO)>0){
					this.disabledSaveConfeccion  =false;
				}else{
					this.disabledSaveConfeccion = true;
				}
			}else{
				detailSelected_estampado.setSubValorFactura(detailSelected_estampado.getValorFinal().multiply(detailSelected_estampado.getUnidades()!=null?detailSelected_estampado.getUnidades():BigDecimal.ZERO));
				PrimeFaces.current().ajax().update("dialogs:idTabView:subTotalSimpleEstampado");
				if(detailSelected_estampado.getSubValorFactura()!=null &&
						detailSelected_estampado.getSubValorFactura().compareTo(BigDecimal.ZERO)>0){
					this.disabledSaveEstampados =false;
				}else{
					this.disabledSaveEstampados = true;
				}
				PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFacturaEstampado");
			}
		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR BLUR SIMPLE UNIDADES ",e);
		}
	}

	public void blurUnidades() {
		try {
			//detailSelected.setTotal(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
			detailSelected.setSubValorFactura(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
			setDisabledSave(false);
			PrimeFaces.current().ajax().update("dialogs:idTabView:subTotal");
			PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO BLUR UNIDADES ",e);
		}
	}

	public void saveEstampados(){
		try{

		}catch (Exception e){
			log.log(Level.SEVERE, "ERROR EN EL DETALLE DE ESTAMPADOS",e);
		}
	}

	public void saveProduct(String option) {
		try {
			if (!editMode) {
				if (this.validatePreSendBill(option)) {
					//detailSelected.setTotal(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
					if(option.equals("E")){
						detailSelected_estampado.setFecha(new Date());
						detailSelected_estampado.setTipo("E");
						this.bill.getLstDetailBill().add(detailSelected_estampado);
						this.bill.setTotal(bill.getTotal().add(this.detailSelected_estampado.getSubValorFactura()));
					}else if (option.equals("C")){
						detailSelected_confecciones.setFecha(new Date());
						detailSelected_confecciones.setTipo("C");
						this.bill.getLstDetailBill().add(detailSelected_confecciones);
						this.bill.setTotal(bill.getTotal().add(this.detailSelected_confecciones.getSubValorFactura()));
					}else{
						detailSelected.setFecha(new Date());
						detailSelected.setTipo("B");
						this.bill.getLstDetailBill().add(detailSelected);
						this.bill.setTotal(bill.getTotal().add(this.detailSelected.getSubValorFactura()));
					}
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
	
	
	private Boolean validatePreSendBill(String option) {
		try {
			if("E".equals(option)){
				if(this.detailSelected_estampado.getValorFinal()==null || this.detailSelected_estampado.getUnidades()== null) {
					return false;
				}
			}else if("C".equals(option)){
				if(this.detailSelected_confecciones.getValorFinal()==null || this.detailSelected_confecciones.getUnidades()== null) {
					return false;
				}
			}else{
				if(this.detailSelected.getValorFinal()==null || this.detailSelected.getUnidades()== null) {
					return false;
				}
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
			parametros.put("total", bill.getTotal()+"");

			BigDecimal totalAbonos = bill.getLstAbonos().stream().map(Abono::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal saldo =bill.getTotal().subtract(totalAbonos);

			parametros.put("abonos", totalAbonos+"");
			parametros.put("saldo", saldo+"");

			//parametros.put("ds",new  net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(lstDetailBill));
			parametros.put("ds",new  net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(bill.getLstDetailBill()));
			parametros.put("dsAbono",new  net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(bill.getLstAbonos()));
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
			PrimeFaces.current().ajax().update("dialogs:idTabView:totalId");
			PrimeFaces.current().ajax().update("dialogs:idTabView:valorFinalId");
			PrimeFaces.current().ajax().update("dialogs:idTabView:subTotal");
			setDisabledSave(true);
            PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
            PrimeFaces.current().ajax().update("dialogs:idTabView:btnCalcular");
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
				PrimeFaces.current().ajax().update("dialogs:idTabView:valorFinalId");
				PrimeFaces.current().ajax().update("dialogs:idTabView:subTotal");
				setDisabledSave(true);
				PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
			}else {
				this.detailSelected.setValorUnitario(null);
				this.detailSelected.setTotal(null);
				this.detailSelected.setValorFinal(null);
				PrimeFaces.current().ajax().update("dialogs:idTabView:valorUnitarioId");
				PrimeFaces.current().ajax().update("dialogs:idTabView:totalId");
				PrimeFaces.current().ajax().update("dialogs:idTabView:valorFinalId");
				setDisabledSave(true);
				PrimeFaces.current().ajax().update("dialogs:idTabView:btnSendFactura");
				PrimeFaces.current().ajax().update("dialogs:idTabView:btnCalcular");
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

	public DetailBill getDetailSelected_estampado() {
		return detailSelected_estampado;
	}
	public void setDetailSelected_estampado(DetailBill detailSelected_estampado) {
		this.detailSelected_estampado = detailSelected_estampado;
	}

	public Boolean getDisabledSaveEstampados() {
		return disabledSaveEstampados;
	}

	public void setDisabledSaveEstampados(Boolean disabledSaveEstampados) {
		this.disabledSaveEstampados = disabledSaveEstampados;
	}

	public DetailBill getDetailSelected_confecciones() {
		return detailSelected_confecciones;
	}

	public void setDetailSelected_confecciones(DetailBill detailSelected_confecciones) {
		this.detailSelected_confecciones = detailSelected_confecciones;
	}

	public Boolean getDisabledSaveConfeccion() {
		return disabledSaveConfeccion;
	}

	public void setDisabledSaveConfeccion(Boolean disabledSaveConfeccion) {
		this.disabledSaveConfeccion = disabledSaveConfeccion;
	}

	public String getIndexTab() {
		return indexTab;
	}

	public void setIndexTab(String indexTab) {
		this.indexTab = indexTab;
	}

	public String getValueAutocomplete() {
		return valueAutocomplete;
	}

	public void setValueAutocomplete(String valueAutocomplete) {
		this.valueAutocomplete = valueAutocomplete;
	}

	public ResponseCJ getResponseWS() {
		return responseWS;
	}

	public void setResponseWS(ResponseCJ responseWS) {
		this.responseWS = responseWS;
	}
}
