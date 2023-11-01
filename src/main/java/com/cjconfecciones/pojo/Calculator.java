package com.cjconfecciones.pojo;

import java.math.BigDecimal;

public class Calculator {

    private String diseño;
    private BigDecimal puntadas;
    private BigDecimal valorUnitario;
    private BigDecimal total;
    private BigDecimal valorFinal;

    public String getDiseño() {
        return diseño;
    }

    public void setDiseño(String diseño) {
        this.diseño = diseño;
    }

    public BigDecimal getPuntadas() {
        return puntadas;
    }

    public void setPuntadas(BigDecimal puntadas) {
        this.puntadas = puntadas;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }
}
