package com.cjconfecciones.pojo;

import java.math.BigDecimal;
import java.util.Date;

public class DetailBill {
	
	private BigDecimal unidades;
	private String descripcion;
	private BigDecimal valorUnitario;
	private BigDecimal total;
	private Date fecha;
	private BigDecimal puntadas;
	private BigDecimal valorFinal;
	
	public BigDecimal getUnidades() {
		return unidades;
	}
	public void setUnidades(BigDecimal unidades) {
		this.unidades = unidades;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public BigDecimal getPuntadas() {
		return puntadas;
	}
	public void setPuntadas(BigDecimal puntadas) {
		this.puntadas = puntadas;
	}
	public BigDecimal getValorFinal() {
		return valorFinal;
	}
	public void setValorFinal(BigDecimal valorFinal) {
		this.valorFinal = valorFinal;
	}
}
