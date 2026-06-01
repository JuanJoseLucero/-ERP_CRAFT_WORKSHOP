package com.cjconfecciones.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Cliente {
    private String error;
    private String mensaje;
    private String cedula;
    private String nombre;
    private String telefono;
    private String direccion;
    private String email;
}
