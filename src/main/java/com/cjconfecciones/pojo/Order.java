package com.cjconfecciones.pojo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
	
	private Integer id;
	private String fechaEntrega;
	private BigDecimal total;
	private String nombre;
	private String direccion;
	private String telefono;
	private String detalle;
}
