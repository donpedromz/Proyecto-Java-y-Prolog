/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.controllers;

import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.services.SpecialistService;
import donpedromz.integracion_prolog.ui.DiagnosticFrame;
import donpedromz.integracion_prolog.ui.FilterFrame;
import donpedromz.integracion_prolog.ui.SymptomSelectionFrame;
import donpedromz.integracion_prolog.ui.EntryFrame;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author juanp
 */
public class SpecialistController {

    private EntryFrame entryFrame;
    private SymptomSelectionFrame diagnoseFrame;
    private DiagnosticFrame diagnosticFrame;
    private FilterFrame filterFrame;
    private List<Diagnostic> currentDiagnostics;
    private Patient currentPatient;
    private SpecialistService service;

    public SpecialistController(SpecialistService service) {
        this.service = service;
    }

    public void handleClick(java.awt.event.MouseEvent evt) {
        Object source = evt.getSource();
        if (source == this.entryFrame.getjPanel2()) {
            handleDiagnoseButtonClicked();
        }
        if (source == this.diagnoseFrame.getGenerateDiagnoseButton()) {
            try {
                handleCreateDiagnoseRequest();
            } catch (Exception e) {
                String mensaje = e.getMessage();
                if (mensaje == null || mensaje.isEmpty()) {
                    mensaje = "Ha ocurrido un error inesperado";
                }
                JOptionPane.showMessageDialog(
                        null,
                        mensaje,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    public EntryFrame getEntryFrame() {
        return entryFrame;
    }

    public void setEntryFrame(EntryFrame entryFrame) {
        this.entryFrame = entryFrame;
    }

    public SpecialistService getService() {
        return service;
    }

    public void setService(SpecialistService service) {
        this.service = service;
    }

    private void handleDiagnoseButtonClicked() {
        List<Symptom> symptoms = this.service.getSymptoms();
        this.entryFrame.dispose();
        this.diagnoseFrame = new SymptomSelectionFrame(symptoms, this);
        displayFrame(diagnoseFrame);
    }

    private void displayFrame(JFrame frame) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    public void setDiagnoseFrame(SymptomSelectionFrame diagnoseFrame) {
        this.diagnoseFrame = diagnoseFrame;
    }

    private void handleCreateDiagnoseRequest() throws IllegalArgumentException {
        String nombrePaciente = this.diagnoseFrame.getCampoNombre().getText();
        String edadPaciente = this.diagnoseFrame.getCampoEdad().getText();
        validateStringWithoutAccents(nombrePaciente);
        Integer edadPacienteInt = null;
        try{
            edadPacienteInt = Integer.parseInt(edadPaciente);
            if(edadPacienteInt < 0 || edadPacienteInt > 100){
                throw new IllegalArgumentException("Se debe ingresar una edad entre 0 y 100");
            }
        }catch(NumberFormatException e){
            throw new IllegalArgumentException("Se debe ingresar un numero entero");
        }
        List<Symptom> selectedSymptoms = this.diagnoseFrame.getSelectedSymptoms();
        if(selectedSymptoms.isEmpty()){
            throw new IllegalArgumentException("Debes seleccionar al menos 1 sintoma!!");
        }
        this.currentPatient = new Patient(0, nombrePaciente.trim(), edadPacienteInt);
        List<Diagnostic> diagnostics = this.service.getDiagnostics(selectedSymptoms);
        if(diagnostics.isEmpty()){
            JOptionPane.showMessageDialog(
                    null,
                    "No se han encontrado enfermedades asociadas a los sintomas ingresados",
                    "Resultado del Diagnostico",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
        else{
            // Cerrar la selección de síntomas y abrir el frame de diagnósticos
            this.diagnoseFrame.dispose();
            this.currentDiagnostics = diagnostics;
            // Asignar paciente al diagnóstico antes de mostrar
            for (Diagnostic d : this.currentDiagnostics) {
                d.setPatient(this.currentPatient);
            }
            this.diagnosticFrame = new DiagnosticFrame(this);
            this.diagnosticFrame.loadDiagnostics(this.currentDiagnostics, selectedSymptoms);
            displayFrame(this.diagnosticFrame);
        }
    }

    public void handleFilterButtonClicked() {
        if (this.diagnosticFrame == null || this.currentDiagnostics == null) {
            return;
        }
        this.filterFrame = new FilterFrame(this, this.currentDiagnostics);
        displayFrame(this.filterFrame);
    }

    public void handleApplyFilterClicked() {
        if (this.filterFrame == null) {
            return;
        }
        String rawCategory = this.filterFrame.getCategoryField().getText();
        try {
            String normalizedCategory = normalizeCategory(rawCategory);
            List<Diagnostic> filtered = this.service.filterDiagnosticsByCategory(this.currentDiagnostics, normalizedCategory);
            if (filtered.isEmpty()) {
                throw new IllegalArgumentException("La categoria no esta presente en el diagnostico actual");
            }
            this.filterFrame.showFilteredDiagnostics(filtered);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage() != null ? ex.getMessage() : "Categoria invalida",
                    "Error de validacion",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void handlePersistDataClicked() {
        if (this.currentDiagnostics == null || this.currentDiagnostics.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay diagnosticos para guardar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (this.currentPatient == null) {
            JOptionPane.showMessageDialog(null, "No hay datos de paciente para guardar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            this.service.persistDiagnostics(this.currentPatient, this.currentDiagnostics);
            JOptionPane.showMessageDialog(null, "Diagnosticos almacenados correctamente", "Exito", JOptionPane.INFORMATION_MESSAGE);
            if (this.diagnosticFrame != null) {
                this.diagnosticFrame.dispose();
            }
            if (this.filterFrame != null) {
                this.filterFrame.dispose();
                this.filterFrame = null;
            }
            this.entryFrame = new EntryFrame(this);
            displayFrame(this.entryFrame);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage() != null ? ex.getMessage() : "Error al guardar diagnosticos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void validateStringWithoutAccents(String stringToValidate) throws IllegalArgumentException{
        if (stringToValidate == null || stringToValidate.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        stringToValidate = stringToValidate.trim();
        if (stringToValidate.length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener mas de 3 caracteres");
        }
        if (stringToValidate.length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder los 100 caracteres");
        }
        if (!stringToValidate.matches("[a-zA-Z ]+")) {
            throw new IllegalArgumentException("El nombre no puede contener caracteres especiales ni acentos");
        }
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("La categoria no puede estar vacia");
        }
        String trimmed = category.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La categoria no puede estar vacia");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("La categoria no puede exceder los 100 caracteres");
        }
        if (!trimmed.matches("[a-zA-Z ]+")) {
            throw new IllegalArgumentException("La categoria solo puede contener letras y espacios");
        }
        return trimmed;
    }
}
