package com.cjconfecciones.utils;


public enum EnumCJ {

    ESTADO_ABIERTO ("A","ORDEN DE TRABAJO ABIERTA"),
    ERROR ("1","TRANSACCION INCORRECTA"),
    OK ("0","TRANSACCION CORRECTA");

    private String estado;
    private String descripcion;

    EnumCJ(String estado, String descripcion) {
        this.estado = estado;
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
