package donpedromz.integracion_prolog.entities;

import java.util.List;

/**
 * Representa un diagnóstico emitido para un paciente.
 * Contiene la enfermedad detectada (con sus síntomas, categoría y recomendaciones),
 * los síntomas ingresados por el usuario y la referencia al paciente.
 * 
 * @author milomnz
 * @author juanp
 */
public class Diagnostic {
    private int id;
    private Patient patient;
    private Disease disease;
    private List<Symptom> inputSymptoms;

    /**
     * Constructor mínimo.
     */
    public Diagnostic() {
    }

    /**
     * Constructor completo para crear un diagnóstico.
     * 
     * @param id Identificador del diagnóstico
     * @param patient Paciente al que pertenece el diagnóstico
     * @param disease Enfermedad diagnosticada (incluye categoría, síntomas y recomendaciones)
     * @param inputSymptoms Síntomas que ingresó el usuario para obtener el diagnóstico
     */
    public Diagnostic(int id, Patient patient, Disease disease, List<Symptom> inputSymptoms) {
        this.id = id;
        this.patient = patient;
        this.disease = disease;
        this.inputSymptoms = inputSymptoms;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Disease getDisease() {
        return disease;
    }

    public void setDisease(Disease disease) {
        this.disease = disease;
    }

    public List<Symptom> getInputSymptoms() {
        return inputSymptoms;
    }

    public void setInputSymptoms(List<Symptom> inputSymptoms) {
        this.inputSymptoms = inputSymptoms;
    }

    // --- Métodos de conveniencia para acceder a datos de Disease ---

    /**
     * @return Nombre de la enfermedad diagnosticada
     */
    public String getDiseaseName() {
        return disease != null ? disease.getName() : null;
    }

    /**
     * @return Nombre de la categoría de la enfermedad
     */
    public String getCategoryName() {
        return disease != null && disease.getCategory() != null 
                ? disease.getCategory().getName() 
                : null;
    }

    /**
     * @return Lista de síntomas propios de la enfermedad
     */
    public List<Symptom> getDiseaseSymptoms() {
        return disease != null ? disease.getSymptoms() : null;
    }

    /**
     * @return Lista de recomendaciones de la enfermedad
     */
    public List<Recomendation> getRecommendations() {
        return disease != null ? disease.getRecomendations() : null;
    }
}

