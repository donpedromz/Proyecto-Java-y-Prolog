package donpedromz.integracion_prolog.services;

import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.repositories.DiagnosticRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ICategoryRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiseaseRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ISymptomRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jpl7.Query;
import org.jpl7.Term;
import donpedromz.integracion_prolog.shared.FormatUtils;
import donpedromz.integracion_prolog.shared.PrologQueryExecutor;

/**
 * Servicio especialista que actúa como puente entre Java y el motor de inferencia Prolog.
 * @author milomnz
 * @author juanp
 */
public class SpecialistService {

    private ICategoryRepository categoryRepository;
    private IDiseaseRepository diseaseRepository;
    private ISymptomRepository symptomRepository;
    private DiagnosticRepository diagnosticRepository = new DiagnosticRepository();

    /**
     * @param categoryRepository Repositorio para categorías
     * @param diseaseRepository Repositorio para enfermedades
     * @param symptomRepository Repositorio para síntomas
     */
    public SpecialistService(
            ICategoryRepository categoryRepository, 
            IDiseaseRepository diseaseRepository,
            ISymptomRepository symptomRepository) {
        this.categoryRepository = categoryRepository;
        this.diseaseRepository = diseaseRepository;
        this.symptomRepository = symptomRepository;
    }

    /**
     * Identifica enfermedad por un síntoma individual.
     */
    public List<Disease> identifyDiseasesBySymptom(Symptom symptom) {
        List<Disease> diseases = new ArrayList<>();
        String symptomSlug = FormatUtils.slug(symptom.getDescription());

        String queryString = String.format(
                "identificar_enfermedad('%s', Id, Enfermedad)",
                symptomSlug
        );

        executeQuery(queryString, solution -> {
            int id = solution.get("Id").intValue();
            String name = solution.get("Enfermedad").name();
            diseases.add(new Disease(id, name));
        });

        return diseases;
    }
    
    /**
     * Encuentra enfermedades que coinciden con TODOS los síntomas proporcionados.
     * @param symptoms Lista de síntomas del paciente
     * @return Lista de nombres de enfermedades que coinciden
     */
    public List<String> getMatchingDiseases(List<Symptom> symptoms) {
        List<String> matchingDiseases = new ArrayList<>();

        List<String> slugList = FormatUtils.toSlugList(symptoms);

        String prologList = "[" + slugList.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", ")) + "]";

        String queryString = String.format(
                "coincide_sintomas(%s, Enfermedad)",
                prologList
        );

        executeQuery(queryString, solution -> {
            matchingDiseases.add(solution.get("Enfermedad").name());
        });

        return matchingDiseases;
    }
    
    /**
     * Obtiene todas las enfermedades de una categoría específica.
     * @param categoryName Nombre de la categoría (viral, cronica, digestiva)
     * @return Lista de nombres de enfermedades en esa categoría
     */
    public List<String> getDiseasesByCategory(String categoryName) {
        List<String> diseases = new ArrayList<>();
        String categorySlug = FormatUtils.slug(categoryName);
        String queryString = String.format("enfermedades_por_categoria('%s', Enfermedad)", categorySlug);
        executeQuery(queryString, solution -> {
            diseases.add(solution.get("Enfermedad").name());
        });
        return diseases;
    }
    
    /**
     * Obtiene todas las enfermedades crónicas.
     * @return Lista de nombres de enfermedades crónicas
     */
    public List<String> getChronicDiseases() {
        List<String> chronicDiseases = new ArrayList<>();

        String queryString = "enfermedades_cronicas(Enfermedad)";
        executeQuery(queryString, solution -> chronicDiseases.add(solution.get("Enfermedad").name()));
        return chronicDiseases;
    }
    
    /**
     * Realiza un diagnóstico completo basándose en los síntomas del paciente.
     * @param inputSymptoms Lista de síntomas que presenta el paciente
     * @return Lista de diagnósticos posibles con enfermedad completa (id, nombre, síntomas, categoría, recomendaciones)
     */
    public List<Diagnostic> getDiagnostics(List<Symptom> inputSymptoms) {
        List<Symptom> uniqueInputSymptoms = dedupeSymptoms(inputSymptoms);

        // Convertir síntomas a slugs para construir la consulta Prolog
        List<String> slugList = FormatUtils.toSlugList(uniqueInputSymptoms);
        String prologList = "[" + slugList.stream()
            .map(s -> "'" + s + "'")
            .collect(Collectors.joining(", ")) + "]";

        String queryString = String.format(
            "diagnostico(%s, Id, Enfermedad, SintomasEnfermedad, Categoria, Recomendacion)",
            prologList
        );

        // Delegar la ejecución y el formateo a PrologQueryExecutor
        List<Diagnostic> diagnostics = PrologQueryExecutor.getDiagnostics(queryString, uniqueInputSymptoms);
        return dedupeDiagnostics(diagnostics);
    }
    public List<Symptom> getSymptoms(){
        return this.symptomRepository.getAll();
    }

    public List<Diagnostic> filterDiagnosticsByCategory(List<Diagnostic> diagnostics, String category) {
        if (diagnostics == null) {
            return new ArrayList<>();
        }
        String normalizedSlug = FormatUtils.slug(category);
        List<Diagnostic> filtered = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d == null || d.getDisease() == null || d.getDisease().getCategory() == null) {
                continue;
            }
            String diseaseCategory = d.getDisease().getCategory().getName();
            String diseaseCategorySlug = FormatUtils.slug(diseaseCategory);
            if (normalizedSlug.equals(diseaseCategorySlug)) {
                filtered.add(d);
            }
        }
        List<Diagnostic> uniques = dedupeDiagnostics(filtered);
        if (uniques.isEmpty()) {
            throw new IllegalArgumentException("La categoria no esta presente en el diagnostico actual");
        }
        return uniques;
    }

    public void persistDiagnostics(Patient patient, List<Diagnostic> diagnostics) {
        if (patient == null) {
            throw new IllegalArgumentException("Paciente invalido");
        }
        if (diagnostics == null || diagnostics.isEmpty()) {
            throw new IllegalArgumentException("No hay diagnosticos para guardar");
        }
        diagnosticRepository.saveAll(diagnostics);
        exportDiagnosticsCsv(patient, diagnostics);
    }

    private void exportDiagnosticsCsv(Patient patient, List<Diagnostic> diagnostics) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedName = safe(patient.getName()).replace(" ", "_");
        String fileName = "diagnosticos_" + sanitizedName + "_edad" + patient.getAge() + "_" + timestamp + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("patient_name,patient_age,disease_id,disease_name,category,symptoms,recomendations,input_symptoms\n");
            for (Diagnostic d : diagnostics) {
                String symptoms = joinSymptomsCsv(d.getDiseaseSymptoms());
                String recs = joinRecsCsv(d.getRecommendations());
                String input = joinSymptomsCsv(d.getInputSymptoms());
                writer.write(String.format("%s,%d,%d,%s,%s,%s,%s,%s\n",
                        safe(patient.getName()),
                        patient.getAge(),
                        d.getDisease() != null ? d.getDisease().getId() : 0,
                        safe(d.getDiseaseName()),
                        safe(d.getCategoryName()),
                        safe(symptoms),
                        safe(recs),
                        safe(input)
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar CSV", e);
        }
    }

    private String joinSymptomsCsv(List<Symptom> symptoms) {
        if (symptoms == null) return "";
        return symptoms.stream()
                .map(s -> s != null ? s.getDescription() : "")
                .collect(Collectors.joining("|"));
    }

    private String joinRecsCsv(List<donpedromz.integracion_prolog.entities.Recomendation> recs) {
        if (recs == null) return "";
        return recs.stream()
                .map(r -> r != null ? r.getDescription() : "")
                .collect(Collectors.joining("|"));
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").replace(",", " ").trim();
    }
    
    private List<Symptom> dedupeSymptoms(List<Symptom> symptoms) {
        List<Symptom> uniques = new ArrayList<>();
        List<Integer> seenIds = new ArrayList<>();
        for (Symptom s : symptoms) {
            Integer id = s != null ? s.getId() : null;
            if (id == null || !seenIds.contains(id)) {
                if (id != null) {
                    seenIds.add(id);
                }
                uniques.add(s);
            }
        }
        return uniques;
    }
    
    private List<Diagnostic> dedupeDiagnostics(List<Diagnostic> diagnostics) {
        List<Diagnostic> uniques = new ArrayList<>();
        List<Integer> seenDiseaseIds = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            Integer diseaseId = (d != null && d.getDisease() != null) ? d.getDisease().getId() : null;
            if (diseaseId == null || !seenDiseaseIds.contains(diseaseId)) {
                if (diseaseId != null) {
                    seenDiseaseIds.add(diseaseId);
                }
                uniques.add(d);
            }
        }
        return uniques;
    }
    /**
     * Helper funcional para ejecutar consultas
     */
    private void executeQuery(String queryString, Consumer<Map<String, Term>> solutionProcessor) {
        Query query = new Query(queryString);

        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            solutionProcessor.accept(solution);
        }
        query.close();
    }

    public ICategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public IDiseaseRepository getDiseaseRepository() {
        return diseaseRepository;
    }

    public ISymptomRepository getSymptomRepository() {
        return symptomRepository;
    }
}
