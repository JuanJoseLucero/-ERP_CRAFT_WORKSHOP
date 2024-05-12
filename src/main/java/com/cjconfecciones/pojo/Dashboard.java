package com.cjconfecciones.pojo;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Dashboard {
    private BigDecimal cobrado;
    private String error;
    private BigDecimal nroPedidos;
    private BigDecimal porCobrar;
    private BigDecimal total;
}
