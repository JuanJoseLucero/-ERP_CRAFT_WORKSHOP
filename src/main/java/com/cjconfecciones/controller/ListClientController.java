package com.cjconfecciones.controller;

import com.cjconfecciones.pojo.Cliente;
import com.cjconfecciones.pojo.ListaCliente;
import com.cjconfecciones.utils.ApiRestClient;
import com.cjconfecciones.utils.EnumCJ;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class ListClientController implements Serializable {

    @Inject
    private ApiRestClient apiRestClient;

    Logger log = Logger.getLogger(ListClientController.class.getName());

    private List<Cliente> clientes;
    private Cliente selectedCliente;
    private String cedulaFilter;
    private String nombreFilter;

    @PostConstruct
    public void init() {
        try {
            selectedCliente = new Cliente();
            listar();
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR INITIALIZE", e);
        }
    }

    public void listar() {
        try {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            jsonBuilder.add("dummy", "");
            ListaCliente response = apiRestClient.consumeWebServices(ListaCliente.class, "cliente/list", jsonBuilder.build().toString());
            if (response != null && EnumCJ.OK.getEstado().equals(response.getError())) {
                clientes = response.getPersonas();
            } else {
                clientes = List.of();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR LISTAR", e);
        }
    }

    public void nuevo() {
        selectedCliente = new Cliente();
        PrimeFaces.current().ajax().update("clientForm:clientDialogContent");
        PrimeFaces.current().executeScript("PF('clientDialog').show()");
    }

    public void guardar() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            jsonBuilder.add("cedula", selectedCliente.getCedula() != null ? selectedCliente.getCedula() : "");
            jsonBuilder.add("nombre", selectedCliente.getNombre() != null ? selectedCliente.getNombre() : "");
            jsonBuilder.add("telefono", selectedCliente.getTelefono() != null ? selectedCliente.getTelefono() : "");
            jsonBuilder.add("direccion", selectedCliente.getDireccion() != null ? selectedCliente.getDireccion() : "");
            jsonBuilder.add("email", selectedCliente.getEmail() != null ? selectedCliente.getEmail() : "");

            boolean editando = false;
            if (selectedCliente.getCedula() != null && clientes != null) {
                editando = clientes.stream().anyMatch(c -> c.getCedula() != null && c.getCedula().equals(selectedCliente.getCedula()));
            }

            String resource = editando ? "cliente/update" : "cliente/create";
            JsonObject response = apiRestClient.consumeWebServices(JsonObject.class, resource, jsonBuilder.build().toString());

            if (response != null && response.containsKey("error") && "0".equals(response.getString("error"))) {
                String msg = editando ? "Cliente actualizado exitosamente" : "Cliente creado exitosamente";
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));
                listar();
                PrimeFaces.current().ajax().update("clientForm:clientesIdTable");
                PrimeFaces.current().executeScript("PF('clientDialog').hide()");
                PrimeFaces.current().ajax().update("general:messages");
            } else {
                String msg = response != null && response.containsKey("mensaje")
                        ? response.getString("mensaje") : "Error al guardar el cliente";
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
                PrimeFaces.current().ajax().update("general:messages");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR GUARDAR", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar el cliente"));
            PrimeFaces.current().ajax().update("general:messages");
        }
    }

    public void editar(String cedula) {
        try {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            jsonBuilder.add("cedula", cedula);
            JsonObject response = apiRestClient.consumeWebServices(JsonObject.class, "cliente/get", jsonBuilder.build().toString());

            if (response != null && response.containsKey("error") && "0".equals(response.getString("error"))) {
                selectedCliente.setCedula(response.getString("cedula", ""));
                selectedCliente.setNombre(response.getString("nombre", ""));
                selectedCliente.setTelefono(response.getString("telefono", ""));
                selectedCliente.setDireccion(response.getString("direccion", ""));
                selectedCliente.setEmail(response.getString("email", ""));
                PrimeFaces.current().ajax().update("clientDialogForm:clientDialogContent");
                PrimeFaces.current().executeScript("PF('clientDialog').show()");
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cliente no encontrado"));
                PrimeFaces.current().ajax().update("general:messages");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR EDITAR", e);
        }
    }

    public void eliminar(String cedula) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            jsonBuilder.add("cedula", cedula);
            JsonObject response = apiRestClient.consumeWebServices(JsonObject.class, "cliente/delete", jsonBuilder.build().toString());

            if (response != null && response.containsKey("error") && "0".equals(response.getString("error"))) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Cliente eliminado exitosamente"));
                listar();
                PrimeFaces.current().ajax().update("clientForm:clientesIdTable");
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al eliminar el cliente"));
            }
            PrimeFaces.current().ajax().update("general:messages");
        } catch (Exception e) {
            log.log(Level.SEVERE, "ERROR ELIMINAR", e);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al eliminar el cliente"));
            PrimeFaces.current().ajax().update("general:messages");
        }
    }

    public List<Cliente> getClientes() { return clientes; }
    public void setClientes(List<Cliente> clientes) { this.clientes = clientes; }
    public Cliente getSelectedCliente() { return selectedCliente; }
    public void setSelectedCliente(Cliente selectedCliente) { this.selectedCliente = selectedCliente; }
    public String getCedulaFilter() { return cedulaFilter; }
    public void setCedulaFilter(String cedulaFilter) { this.cedulaFilter = cedulaFilter; }
    public String getNombreFilter() { return nombreFilter; }
    public void setNombreFilter(String nombreFilter) { this.nombreFilter = nombreFilter; }
}
