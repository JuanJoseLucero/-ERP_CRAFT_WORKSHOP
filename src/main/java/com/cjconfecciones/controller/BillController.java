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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class BillController implements Serializable {

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
			if(sessionController.getBillSesion().getPedidoId()!=null) {
				log.info("-----------".concat(sessionController.getBillSesion().getPedidoId()));
				Bill billSaved = apiRestClient.consumeWebServices(Bill.class, "order/getOrderById",util.converterJson(sessionController.getBillSesion()));
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				billSaved.setFechaDate(sdf.parse(billSaved.getFecha()));
				bill = billSaved;
			}
			this.bill.setTotal(BigDecimal.ZERO);
			disabledSave = true;
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR TO INIT CONTROLLER ",e);
		}
	}
	
	public void  searchClient() {
		log.info("objecto " .concat(util.converterJson(bill)));
		Bill responseWS =  apiRestClient.consumeWebServices(Bill.class, "order/searchClient",util.converterJson(bill));
		//log.info(responseWS.getNombres());
		//this.bill.setDireccion(responseWS.getDireccion());
		if(responseWS.getIdentificacion()!=null) {
			this.bill = responseWS;
			this.bill.setTotal(BigDecimal.ZERO);
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
				String objecto = util.converterJson(bill);
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				bill.setFecha(formatter.format(bill.getFechaDate()));
				log.info("objecto " .concat(objecto));
				ResponseCJ responseWS =  apiRestClient.consumeWebServices(ResponseCJ.class, "order/new",util.converterJson(bill));
				if(responseWS.getError().equals("0")){
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "EXITOSO", "SE ALMACENO CORRECTAMENTE"));
					this.generateBill();
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
                this.detailSelected.setTotal((detailSelected.getPuntadas().multiply(detailSelected.getValorUnitario())).divide(new BigDecimal("1000")) );
                setDisabledSave(false);
                PrimeFaces.current().ajax().update("dialogs:btnSendFactura");
                PrimeFaces.current().ajax().update("dialogs:btnCalcular");
                //this.detailSelected.setValorFinal((detailSelected.getPuntadas().multiply(detailSelected.getValorUnitario())).divide(new BigDecimal("1000")));
                //log.info(this.calculator.getTotal()+"");
            //}
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO CALCULATED VALUES ",e);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Intente nuevamente"));
        }
    }
	
	public void saveProduct() {
		try {
			if (!editMode) {
				if (this.validatePreSendBill()) {
					detailSelected.setTotal(detailSelected.getUnidades().multiply(detailSelected.getValorFinal()));
					detailSelected.setFecha(new Date());
					this.bill.getLstDetailBill().add(detailSelected);
					this.bill.setTotal(bill.getTotal().add(this.detailSelected.getTotal()));
					PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
					PrimeFaces.current().ajax().update("billForm:total");
					PrimeFaces.current().executeScript("PF('manageProductDialog').hide()");
				}else {
					FacesContext context = FacesContext.getCurrentInstance();
		            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Valor Final y Unidades obligatorios"));
				}
			}
			
		}catch (Exception e) {
			log.log(Level.SEVERE, "ERROR WHEN SAVE DETAIL ",e);
		}
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
		/*
		log.info("Entro al cell edit");
		Object newValue = event.getNewValue();
		BigDecimal unidades = new BigDecimal(String.valueOf(newValue));
		sessionController.getCalculator().setValorFinal(unidades.multiply(sessionController.getCalculator().getValorUnitario()));
		bill.getLstDetailBill().clear();
		List<DetailBill> lstDetailBill = new ArrayList<DetailBill>();
		detail.setDescripcion(sessionController.getCalculator().getDiseño());
		detail.setValorUnitario(sessionController.getCalculator().getValorFinal());
		detail.setTotal(sessionController.getCalculator().getTotal());		
		lstDetailBill.add(detail);
		bill.setLstDetailBill(lstDetailBill);
		
		*/
		
		
		//PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
		
		/*
		log.info("Entro al cell edit");
		 Object newValue = event.getNewValue();
		 BigDecimal unidades = new BigDecimal(String.valueOf(newValue));
		 sessionController.getCalculator().setValorFinal(unidades.multiply(sessionController.getCalculator().getValorUnitario()));
		 bill.getLstDetailBill().get(0).setTotal(sessionController.getCalculator().getValorFinal());
		 log.info(bill.getLstDetailBill().get(0).getTotal()+"");
		 PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
		 
		 */
//        Object oldValue = event.getOldValue();
//        Object newValue = event.getNewValue();
//
//        if (newValue != null && !newValue.equals(oldValue)) {
//        	log.info("Cell Changed Old: " + oldValue + ", New:" + newValue);
//        	
//        }
    }


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
