package com.cjconfecciones.pojo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetailBill {
	
	private Integer id;
	private BigDecimal unidades;
	private String descripcion;
	private BigDecimal puntadas;
	private BigDecimal valorPuntada;
	private BigDecimal valorDisenioCalculado;
	private BigDecimal valorDisenioFinal;
	private BigDecimal total;
	private Date fecha;
	private String fechaCadena;

	/** campos sirven tanto para confecciones como para estamapados**/
	private BigDecimal valorFinal;
	private BigDecimal subValorFactura;
	private String tipo;
}
