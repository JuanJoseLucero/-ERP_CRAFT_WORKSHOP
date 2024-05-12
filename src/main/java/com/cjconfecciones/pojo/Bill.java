package com.cjconfecciones.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
	private String pedidoId;
	private String identificacion;
	private String nombres;
	private Date fechaDate;
	private String fecha;
	private String direccion;
	private String telefono;
	private BigDecimal total;
	private List<DetailBill> lstDetailBill = new ArrayList<DetailBill>();
	private List<Abono> lstAbonos = new ArrayList<>();
}
