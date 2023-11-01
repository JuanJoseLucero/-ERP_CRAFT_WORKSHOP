package com.cjconfecciones;


import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;


@Named
@ViewScoped
public class TestJSF implements Serializable {


    private String saludo;
    @PostConstruct
    public void initExample(){
        System.out.println("Ejemplo de hola");
        saludo = "JJ";
    }

    public String getSaludo() {
        return saludo;
    }

    public void setSaludo(String saludo) {
        this.saludo = saludo;
    }
}
