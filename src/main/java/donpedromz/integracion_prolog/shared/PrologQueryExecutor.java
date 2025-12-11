/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.shared;

import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jpl7.Query;
import org.jpl7.Term;

/**
 * Ejecutor de consultas Prolog. Encapsula la lógica de comunicación con el motor de inferencia.
 * @author juanp
 */
public class PrologQueryExecutor {

    /**
     * Ejecuta la consulta Prolog (ya construida) y devuelve diagnósticos con entidades formateadas.
     * @param queryString consulta completa (ej: diagnostico([fiebre,tos], Id, Enfermedad, ...))
     * @param inputSymptoms síntomas originales ingresados por el usuario
     */
    public static List<Diagnostic> getDiagnostics(String queryString, List<Symptom> inputSymptoms) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();

            int diseaseId = solution.get("Id").intValue();
            String diseaseSlug = solution.get("Enfermedad").name();
            String categorySlug = solution.get("Categoria").name();

            // Síntomas de la enfermedad
            Term symptomsTerm = solution.get("SintomasEnfermedad");
            List<String> symptomSlugList = FormatUtils.prologListToJavaList(symptomsTerm);
            List<Symptom> diseaseSymptoms = FormatUtils.slugsToSymptoms(symptomSlugList);
            // No tenemos ids de síntomas en Prolog; se asigna 0 por defecto

            // Recomendaciones
            Term recsTerm = solution.get("Recomendacion");
            List<String> recSlugList = FormatUtils.prologListToJavaList(recsTerm);
            List<Recomendation> recomendations = FormatUtils.slugsToRecomendations(recSlugList);
            // Id de recomendaciones no viene de Prolog; se deja 0

            // Categoría
            Category category = FormatUtils.slugToCategory(categorySlug);
            // Id de categoría no viene de Prolog; se deja 0

            // Enfermedad completa con nombre presentable
            String diseaseName = FormatUtils.unslug(diseaseSlug);
            Disease disease = new Disease(diseaseId, diseaseName, category, diseaseSymptoms, recomendations);

            // Diagnóstico (sin paciente aún, se asigna después)
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setDisease(disease);
            diagnostic.setInputSymptoms(inputSymptoms);
            diagnostics.add(diagnostic);
        }
        query.close();

        return diagnostics;
    }

    /**
     * Identifica enfermedades dado un síntoma y una regla Prolog.
     */
    public static List<Disease> identifyDiseases(List<Symptom> symptoms, String rule) {
        List<Disease> posibleDiseases = new ArrayList<>();
        Query q = new Query(rule);
        while (q.hasMoreSolutions()) {
            Map<String, Term> solution = q.nextSolution();
            Integer id = null;
            Term idT = solution.get("Id");
            if (idT != null) {
                try {
                    id = Integer.parseInt(idT.toString());
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
            String description = null;
            Term descriptionT = solution.get("Description");
            if (descriptionT != null) {
                description = descriptionT.toString();
            }
            posibleDiseases.add(new Disease(id, description));
        }
        q.close();
        return posibleDiseases;
    }

    /**
     * Crea un hecho dinámico en Prolog.
     */
    public static void createDynamicFact(String fact) {
        Query q = new Query(fact);
        if (q.hasSolution()) {
            System.out.println("Hecho dinamico agregado: " + fact);
        }
        q.close();
    }
}
