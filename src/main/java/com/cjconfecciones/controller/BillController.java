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

import org.primefaces.event.CellEditEvent;

import com.cjconfecciones.pojo.Bill;
import com.cjconfecciones.pojo.DetailBill;
import com.cjconfecciones.utils.GenerateReport;

import jakarta.annotation.PostConstruct;
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
	
	Logger log = Logger.getLogger(BillController.class.getName());
	private Bill bill = new Bill();
	DetailBill detail = new DetailBill();
	
	@PostConstruct	
	private void init() {
		log.info("Se procede a iniciarlizqr el bin de billcontroller");
		
		detail.setDescripcion(sessionController.getCalculator().getDiseño());
		detail.setValorUnitario(sessionController.getCalculator().getValorFinal());
		detail.setTotal(BigDecimal.ZERO);
		List<DetailBill> lstDetailBill = new ArrayList<DetailBill>();
		lstDetailBill.add(detail);
		bill.setLstDetailBill(lstDetailBill);
		log.info("temino el flujo");
		log.info(bill.getLstDetailBill().get(0).getDescripcion());
		//PrimeFaces.current().ajax().update("billForm:detalleFacturaId");
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
	        String dateString = dateFormat.format(bill.getFecha());
					
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
	
	
}
