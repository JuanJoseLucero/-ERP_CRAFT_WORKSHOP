package com.cjconfecciones.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Abono {

    private Integer id;
    private String fecha;
    private BigDecimal valor;
    private Integer ccabecera;
}
