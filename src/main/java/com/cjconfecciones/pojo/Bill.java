package com.cjconfecciones.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bill {
	
	private String identificacion;
	private String nombres;
	private Date fecha;
	private String direccion;
	private String telefono;
	private BigDecimal total;
	
	private List<DetailBill> lstDetailBill = new ArrayList<DetailBill>();
	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public List<DetailBill> getLstDetailBill() {
		return lstDetailBill;
	}
	public void setLstDetailBill(List<DetailBill> lstDetailBill) {
		this.lstDetailBill = lstDetailBill;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public BigDecimal getTotal() {
		return total;
	}
	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
}
