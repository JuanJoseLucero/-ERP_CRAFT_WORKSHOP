package com.cjconfecciones.controller;

import com.cjconfecciones.pojo.Calculator;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.context.PrimeFacesContext;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class CalculatorController implements Serializable {

	
	@Inject
	private SessionController sessionController;
    private Calculator calculator = new Calculator();
    private Logger log = Logger.getLogger(CalculatorController.class.getName());
    @PostConstruct
    public void init(){
        log.info("INIT CALCULATOR CONTROLLER");
    }
    
    public void generateBill() {
    	try {
    		sessionController.setCalculator(this.calculator);
    		FacesContext context = FacesContext.getCurrentInstance();
    	    ExternalContext externalContext = context.getExternalContext();
    	    externalContext.redirect("../documentation/bill.xhtml");
    	}catch (Exception e) {
    		log.log(Level.SEVERE, "ERROR TO REDIRECT BILL ",e);
		}
    }

    public void getCalculatedValues(){
        try{
            log.info("entro a get calculator valores");
            if(validationsFields()){
                this.calculator.setTotal((calculator.getPuntadas().multiply(calculator.getValorUnitario())).divide(new BigDecimal("1000")) );
                this.calculator.setValorFinal((calculator.getPuntadas().multiply(calculator.getValorUnitario())).divide(new BigDecimal("1000")));
                log.info(this.calculator.getTotal()+"");
            }
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO CALCULATED VALUES ",e);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", "Intente nuevamente"));
        }
    }

    FacesMessage msg;
    private Boolean validationsFields(){
        try{
            if (calculator.getDiseño() == null || calculator.getDiseño().trim().isEmpty()){
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Campo requerido", "Falta el campo diseño"));
                return false;
            }else if (calculator.getPuntadas() == null){

            }
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR TO VALIDATIONS ",e);
            return false;
        }
        return true;
    }


    public void testClick(){
        log.info("click test");
    }

    public Calculator getCalculator() {
        return calculator;
    }

    public void setCalculator(Calculator calculator) {
        this.calculator = calculator;
    }
}
