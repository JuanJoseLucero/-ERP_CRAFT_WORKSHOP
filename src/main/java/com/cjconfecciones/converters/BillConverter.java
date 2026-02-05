package com.cjconfecciones.converters;

import com.cjconfecciones.pojo.Bill;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.primefaces.component.autocomplete.AutoComplete;

import java.util.List;

@FacesConverter("billConverter")
public class BillConverter  implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if(value == null || value.isEmpty()){
            return null;
        }
        AutoComplete ac = (AutoComplete) component;
        List<Bill> list = (List<Bill>) ac.getCompleteMethod().invoke(context.getELContext(),new Object[]{ value });
        Bill respuesta = list.stream()
                .filter(p -> p.getNombres().equals(value))
                .findFirst()
                .orElse(null);
        return respuesta;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Bill respuesta) {
            if(respuesta.getIdentificacion()!=null){
                return respuesta.getNombres();
            }else{
                return "";
            }
        }
        return "";
    }
}
