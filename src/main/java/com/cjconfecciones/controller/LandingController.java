package com.cjconfecciones.controller;

import com.cjconfecciones.pojo.Dashboard;
import com.cjconfecciones.utils.ApiRestClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class LandingController implements Serializable {


    @Inject
    private ApiRestClient apiRestClient;

    private Dashboard dashboard;

    Logger log = Logger.getLogger(LandingController.class.getName());

    @PostConstruct
    private void init(){
        try{
            dashboard =apiRestClient.consumeWebServices(Dashboard.class, "dashboard/getDataDashboard", "");

        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR INIT LANDING DASHBOARD ",e);
        }
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }
}
