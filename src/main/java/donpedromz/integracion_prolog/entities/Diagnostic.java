package donpedromz.integracion_prolog.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un diagnóstico emitido para un paciente.
 * Contiene las enfermedades detectadas (con sus síntomas, categoría y recomendaciones),
 * los síntomas ingresados por el usuario y la referencia al paciente.
 * 
 * @author milomnz
 * @author juanp
 */
public class Diagnostic {
    private int id;
    private Patient patient;
    private List<Disease> diseases;
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
     * @param diseases Enfermedades diagnosticadas (incluyen categoría, síntomas y recomendaciones)
     * @param inputSymptoms Síntomas que ingresó el usuario para obtener el diagnóstico
     */
    public Diagnostic(int id, Patient patient, List<Disease> diseases, List<Symptom> inputSymptoms) {
        this.id = id;
        this.patient = patient;
        this.diseases = diseases;
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

    public List<Disease> getDiseases() {
        if (diseases == null) {
            diseases = new ArrayList<>();
        }
        return diseases;
    }

    public void setDiseases(List<Disease> diseases) {
        this.diseases = diseases;
    }

    public List<Symptom> getInputSymptoms() {
        return inputSymptoms;
    }

    public void setInputSymptoms(List<Symptom> inputSymptoms) {
        this.inputSymptoms = inputSymptoms;
    }

    /**
     * Conserva compatibilidad devolviendo la primera enfermedad asociada.
     */
    public Disease getDisease() {
        if (diseases == null || diseases.isEmpty()) {
            return null;
        }
        return diseases.get(0);
    }

    /**
     * Establece una única enfermedad reemplazando la lista previa.
     */
    public void setDisease(Disease disease) {
        if (diseases == null) {
            diseases = new ArrayList<>();
        } else {
            diseases.clear();
        }
        if (disease != null) {
            diseases.add(disease);
        }
    }

    // --- Métodos de conveniencia para acceder a datos de Disease ---

    /**
     * @return Nombre de la enfermedad diagnosticada
     */
    public String getDiseaseName() {
        if (diseases == null || diseases.isEmpty()) {
            return null;
        }
        return diseases.get(0).getName();
    }

    /**
     * @return Nombre de la categoría de la enfermedad
     */
    public String getCategoryName() {
        if (diseases == null || diseases.isEmpty()) {
            return null;
        }
        Category category = diseases.get(0).getCategory();
        return category != null ? category.getName() : null;
    }

    /**
     * @return Lista de síntomas propios de la enfermedad
     */
    public List<Symptom> getDiseaseSymptoms() {
        List<Symptom> all = new ArrayList<>();
        if (diseases == null) {
            return all;
        }
        for (Disease d : diseases) {
            if (d != null && d.getSymptoms() != null) {
                all.addAll(d.getSymptoms());
            }
        }
        return all;
    }

    /**
     * @return Lista de recomendaciones de la enfermedad
     */
    public List<Recomendation> getRecommendations() {
        List<Recomendation> all = new ArrayList<>();
        if (diseases == null) {
            return all;
        }
        for (Disease d : diseases) {
            if (d != null && d.getRecomendations() != null) {
                all.addAll(d.getRecomendations());
            }
        }
        return all;
    }

    /**
     * Nombres de enfermedades separados por coma.
     */
    public String getDiseaseNamesJoined() {
        if (diseases == null || diseases.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diseases.size(); i++) {
            Disease d = diseases.get(i);
            if (d != null && d.getName() != null) {
                sb.append(d.getName());
            }
            if (i < diseases.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Nombres de categorías separados por coma.
     */
    public String getCategoryNamesJoined() {
        if (diseases == null || diseases.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diseases.size(); i++) {
            Disease d = diseases.get(i);
            if (d != null && d.getCategory() != null && d.getCategory().getName() != null) {
                sb.append(d.getCategory().getName());
            }
            if (i < diseases.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}

