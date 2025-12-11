package donpedromz.integracion_prolog.services;

import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.repositories.DiagnosticRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ICategoryRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiseaseRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IRecomendationRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ISymptomRepository;
import donpedromz.integracion_prolog.repositories.RecomendationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private IRecomendationRepository recomendationRepository;
    private DiagnosticRepository diagnosticRepository = new DiagnosticRepository();
    private List<Integer> diagnosticsLoadedIds = new ArrayList<>();
    private boolean diagnosticsLoadedOnce = false;

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
        this.recomendationRepository = new RecomendationRepository();
        ensureDiagnosticsLoadedInProlog();
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
        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            int id = solution.get("Id").intValue();
            String name = solution.get("Enfermedad").name();
            diseases.add(new Disease(id, name));
        }
        query.close();

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
        StringBuilder listBuilder = new StringBuilder("[");
        for (int i = 0; i < slugList.size(); i++) {
            listBuilder.append('\'').append(slugList.get(i)).append('\'');
            if (i < slugList.size() - 1) {
                listBuilder.append(", ");
            }
        }
        listBuilder.append("]");
        String prologList = listBuilder.toString();

        String queryString = String.format(
                "coincide_sintomas(%s, Enfermedad)",
                prologList
        );

        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            matchingDiseases.add(solution.get("Enfermedad").name());
        }
        query.close();

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
        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            diseases.add(solution.get("Enfermedad").name());
        }
        query.close();
        return diseases;
    }
    
    /**
     * Obtiene todas las enfermedades crónicas.
     * @return Lista de nombres de enfermedades crónicas
     */
    public List<String> getChronicDiseases() {
        List<String> chronicDiseases = new ArrayList<>();

        String queryString = "enfermedades_cronicas(Enfermedad)";
        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            chronicDiseases.add(solution.get("Enfermedad").name());
        }
        query.close();
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
        StringBuilder listBuilder = new StringBuilder("[");
        for (int i = 0; i < slugList.size(); i++) {
            listBuilder.append('\'').append(slugList.get(i)).append('\'');
            if (i < slugList.size() - 1) {
                listBuilder.append(", ");
            }
        }
        listBuilder.append("]");
        String prologList = listBuilder.toString();

        String queryString = String.format(
            "diagnostico(%s, Id, Enfermedad, SintomasEnfermedad, Categoria, Recomendacion)",
            prologList
        );

        System.out.println("Ejecutando diagnostico/6 en Prolog con " + uniqueInputSymptoms.size() + " sintomas ingresados");

        List<Diagnostic> diagnostics = PrologQueryExecutor.getDiagnostics(queryString, uniqueInputSymptoms);

        // Limpiar y desduplicar enfermedades por id (o nombre si no viene id).
        List<Disease> diseases = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (Diagnostic d : diagnostics) {
            if (d == null) continue;
            Disease disease = d.getDisease();
            if (disease == null) continue;
            String key = (disease.getId() != 0 ? String.valueOf(disease.getId()) : "") + "|" + (disease.getName() != null ? disease.getName() : "");
            if (seen.add(key)) {
                diseases.add(disease);
            }
        }

        if (diseases.isEmpty()) {
            return new ArrayList<>();
        }

        Diagnostic aggregated = new Diagnostic();
        aggregated.setDiseases(diseases);
        aggregated.setInputSymptoms(uniqueInputSymptoms);
        List<Diagnostic> aggregatedList = new ArrayList<>();
        aggregatedList.add(aggregated);
        System.out.println("Prolog devolvio " + diseases.size() + " enfermedades candidatas para el diagnostico");
        return aggregatedList;
    }
    public List<Symptom> getSymptoms(){
        return this.symptomRepository.getAll();
    }

    public List<Object[]> getTopDiseases(int limit) {
        return diagnosticRepository.topDiseases(limit);
    }

    public List<Object[]> getTopSymptoms(int limit) {
        return diagnosticRepository.topSymptoms(limit);
    }

    public Disease createDisease(String diseaseName, String categoryName, List<String> symptomDescriptions, List<String> recommendationDescriptions) {
        String normalizedDiseaseName = normalizeText(diseaseName, true, "nombre de la enfermedad");
        String normalizedCategoryName = normalizeText(categoryName, false, "categoria");

        if (symptomDescriptions == null || symptomDescriptions.isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar al menos un sintoma");
        }
        if (recommendationDescriptions == null || recommendationDescriptions.isEmpty()) {
            throw new IllegalArgumentException("Debes ingresar al menos una recomendacion");
        }

        List<String> normalizedSymptoms = new ArrayList<>();
        for (String s : symptomDescriptions) {
            normalizedSymptoms.add(normalizeText(s, false, "sintoma"));
        }

        List<String> normalizedRecommendations = new ArrayList<>();
        for (String r : recommendationDescriptions) {
            normalizedRecommendations.add(normalizeText(r, false, "recomendacion"));
        }

        System.out.println("[CreateDisease] Preparando persistencia: nombre=" + normalizedDiseaseName
            + ", categoria=" + normalizedCategoryName
            + ", sintomas=" + normalizedSymptoms
            + ", recomendaciones=" + normalizedRecommendations);

        if (diseaseRepository.getByName(normalizedDiseaseName) != null) {
            throw new IllegalArgumentException("La enfermedad ya se encuentra registrada");
        }

        Category category = categoryRepository.getByName(normalizedCategoryName);
        if (category == null) {
            category = new Category(0, normalizedCategoryName);
            category = categoryRepository.save(category);
            System.out.println("[CreateDisease] Categoria creada id=" + category.getId() + " nombre=" + category.getName());
        } else {
            System.out.println("[CreateDisease] Categoria existente id=" + category.getId() + " nombre=" + category.getName());
        }

        List<Symptom> symptomEntities = new ArrayList<>();
        for (String s : normalizedSymptoms) {
            Symptom symptom = symptomRepository.getByDescription(s);
            if (symptom == null) {
                symptom = symptomRepository.save(new Symptom(0, s));
                System.out.println("[CreateDisease] Sintoma creado id=" + symptom.getId() + " desc=" + symptom.getDescription());
            } else {
                System.out.println("[CreateDisease] Sintoma existente id=" + symptom.getId() + " desc=" + symptom.getDescription());
            }
            symptomEntities.add(symptom);
        }

        List<Recomendation> recEntities = new ArrayList<>();
        for (String r : normalizedRecommendations) {
            Recomendation rec = recomendationRepository.getByDescription(r);
            if (rec == null) {
                rec = recomendationRepository.save(new Recomendation(0, r));
                System.out.println("[CreateDisease] Recomendacion creada id=" + rec.getId() + " desc=" + rec.getDescription());
            } else {
                System.out.println("[CreateDisease] Recomendacion existente id=" + rec.getId() + " desc=" + rec.getDescription());
            }
            recEntities.add(rec);
        }

        Disease disease = new Disease();
        disease.setName(normalizedDiseaseName);
        disease.setCategory(category);
        disease.setSymptoms(symptomEntities);
        disease.setRecomendations(recEntities);

        Disease saved = diseaseRepository.saveDisease(disease);
        System.out.println("[CreateDisease] Enfermedad persistida id=" + saved.getId()
            + " nombre=" + saved.getName()
            + " categoria=" + (saved.getCategory() != null ? saved.getCategory().getName() : "")
            + " sintomasIds=" + extractIds(saved.getSymptoms())
            + " recIds=" + extractIds(saved.getRecomendations()));
        assertDiseaseInProlog(saved);
        return saved;
    }

    public List<Diagnostic> filterDiagnosticsByCategory(List<Diagnostic> diagnostics, String category) {
        if (diagnostics == null) {
            return new ArrayList<>();
        }
        String normalizedSlug = FormatUtils.slug(category);
        List<Diagnostic> filtered = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d == null || d.getDiseases() == null) {
                continue;
            }
            List<Disease> matchedDiseases = new ArrayList<>();
            for (Disease dis : d.getDiseases()) {
                if (dis == null || dis.getCategory() == null) continue;
                String diseaseCategorySlug = FormatUtils.slug(dis.getCategory().getName());
                if (normalizedSlug.equals(diseaseCategorySlug)) {
                    matchedDiseases.add(dis);
                }
            }
            if (!matchedDiseases.isEmpty()) {
                Diagnostic copy = new Diagnostic();
                copy.setId(d.getId());
                copy.setPatient(d.getPatient());
                copy.setInputSymptoms(d.getInputSymptoms());
                copy.setDiseases(matchedDiseases);
                filtered.add(copy);
            }
        }
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("La categoria no esta presente en el diagnostico actual");
        }
        return filtered;
    }

    public void persistDiagnostics(Patient patient, List<Diagnostic> diagnostics) {
        if (patient == null) {
            throw new IllegalArgumentException("Paciente invalido");
        }
        if (diagnostics == null || diagnostics.isEmpty()) {
            throw new IllegalArgumentException("No hay diagnosticos para guardar");
        }
        System.out.println("Guardando diagnosticos en base de datos para paciente: " + patient.getName());
        diagnosticRepository.saveAll(diagnostics);
        ensureDiagnosticsLoadedInProlog();
        exportDiagnosticsCsv(patient, diagnostics);
    }

    public List<Diagnostic> consultDiagnostics(String patientName, int patientAge) {
        if (patientName == null || patientName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (patientAge < 0 || patientAge > 120) {
            throw new IllegalArgumentException("La edad debe estar entre 0 y 120");
        }

        String patientSlug = FormatUtils.slug(patientName.trim());
        String queryString = String.format(
                "consulta_diagnostico('%s', %d, Id)",
                patientSlug,
                patientAge
        );

        List<Integer> diagIds = new ArrayList<>();
        System.out.println("Consultando Prolog (consulta_diagnostico/3) para paciente='" + patientName.trim() + "', edad=" + patientAge);
        Query query = new Query(queryString);
        while (query.hasMoreSolutions()) {
            Map<String, Term> solution = query.nextSolution();
            int diagId = solution.get("Id").intValue();
            if (!diagIds.contains(diagId)) {
                diagIds.add(diagId);
            }
        }
        query.close();

        System.out.println("Prolog devolvio " + diagIds.size() + " diagnosticos para la consulta");

        if (diagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Diagnostic> allDiagnostics = diagnosticRepository.findAll();
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (Diagnostic d : allDiagnostics) {
            if (diagIds.contains(d.getId())) {
                diagnostics.add(d);
            }
        }
        return diagnostics;
    }

    public void ensureDiagnosticsReadyForConsult() {
        ensureDiagnosticsLoadedInProlog();
    }

    private void ensureDiagnosticsLoadedInProlog() {
        List<Diagnostic> diagnostics = diagnosticRepository.findAll();

        // Detectar diagnosticos que faltan por assertear
        List<Diagnostic> missing = new ArrayList<>();
        for (Diagnostic d : diagnostics) {
            if (d == null || d.getDiseases() == null || d.getDiseases().isEmpty()) {
                continue;
            }
            if (!diagnosticsLoadedIds.contains(d.getId())) {
                missing.add(d);
            }
        }

        if (missing.isEmpty()) {
            if (!diagnosticsLoadedOnce) {
                System.out.println("No hay diagnosticos para cargar en Prolog en el arranque.");
                diagnosticsLoadedOnce = true;
            }
            return;
        }

        System.out.println("Cargando diagnosticos en Prolog via assertz: nuevos " + missing.size());
        for (Diagnostic diagnostic : missing) {
            String patientSlug = FormatUtils.slug(diagnostic.getPatient().getName());
            int age = diagnostic.getPatient().getAge();

            String fact = String.format(
                "assertz(diagnostico_paciente(%d, '%s', %d))",
                diagnostic.getId(),
                patientSlug,
                age
            );
            System.out.println("Assertando diagnostico en Prolog: diagId=" + diagnostic.getId() + ", paciente='" + diagnostic.getPatient().getName() + "', edad=" + age);
            System.out.println("[Prolog] Hecho -> " + fact);
            PrologQueryExecutor.createDynamicFact(fact);
            diagnosticsLoadedIds.add(diagnostic.getId());
        }

        diagnosticsLoadedOnce = true;
    }

    private void exportDiagnosticsCsv(Patient patient, List<Diagnostic> diagnostics) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedName = safe(patient.getName()).replace(" ", "_");
        String fileName = "diagnosticos_" + sanitizedName + "_edad" + patient.getAge() + "_" + timestamp + ".csv";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("patient_name,patient_age,diagnostic_id,disease_id,disease_name,category,symptoms,recomendations,input_symptoms\n");
            for (Diagnostic d : diagnostics) {
                List<Disease> diseases = d.getDiseases();
                if (diseases == null || diseases.isEmpty()) {
                    writer.write(String.format("%s,%d,%d,%s,%s,%s,%s,%s,%s\n",
                            safe(patient.getName()),
                            patient.getAge(),
                            d.getId(),
                            0,
                            "",
                            "",
                            "",
                            "",
                            safe(joinSymptomsCsv(d.getInputSymptoms()))
                    ));
                    continue;
                }

                for (Disease disease : diseases) {
                    String symptoms = joinSymptomsCsv(disease != null ? disease.getSymptoms() : null);
                    String recs = joinRecsCsv(disease != null ? disease.getRecomendations() : null);
                    String input = joinSymptomsCsv(d.getInputSymptoms());
                    writer.write(String.format("%s,%d,%d,%d,%s,%s,%s,%s,%s\n",
                            safe(patient.getName()),
                            patient.getAge(),
                            d.getId(),
                            disease != null ? disease.getId() : 0,
                            disease != null ? safe(disease.getName()) : "",
                            (disease != null && disease.getCategory() != null) ? safe(disease.getCategory().getName()) : "",
                            safe(symptoms),
                            safe(recs),
                            safe(input)
                    ));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar CSV", e);
        }
    }

    private String joinSymptomsCsv(List<Symptom> symptoms) {
        if (symptoms == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < symptoms.size(); i++) {
            Symptom symptom = symptoms.get(i);
            String value = symptom != null ? symptom.getDescription() : "";
            sb.append(value);
            if (i < symptoms.size() - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    private String joinRecsCsv(List<donpedromz.integracion_prolog.entities.Recomendation> recs) {
        if (recs == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recs.size(); i++) {
            donpedromz.integracion_prolog.entities.Recomendation rec = recs.get(i);
            String value = rec != null ? rec.getDescription() : "";
            sb.append(value);
            if (i < recs.size() - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    private String safe(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").replace(",", " ").trim();
    }

    private String buildPrologList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append('\'').append(items.get(i)).append('\'');
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
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
    
    public ICategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public IDiseaseRepository getDiseaseRepository() {
        return diseaseRepository;
    }

    public ISymptomRepository getSymptomRepository() {
        return symptomRepository;
    }

    private String normalizeText(String rawValue, boolean allowNumbers, String fieldLabel) {
        if (rawValue == null) {
            throw new IllegalArgumentException("El campo " + fieldLabel + " no puede estar vacio");
        }
        String cleaned = rawValue.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldLabel + " no puede estar vacio");
        }
        if (cleaned.length() > 150) {
            throw new IllegalArgumentException("El campo " + fieldLabel + " no puede exceder los 150 caracteres");
        }
        String pattern = allowNumbers ? "[a-zA-Z0-9 ]+" : "[a-zA-Z ]+";
        if (!cleaned.matches(pattern)) {
            throw new IllegalArgumentException("El campo " + fieldLabel + " no puede contener caracteres especiales ni acentos");
        }
        return cleaned;
    }

    private void assertDiseaseInProlog(Disease disease) {
        if (disease == null) {
            return;
        }
        List<String> symptomSlugs = new ArrayList<>();
        if (disease.getSymptoms() != null) {
            for (Symptom s : disease.getSymptoms()) {
                if (s != null) {
                    symptomSlugs.add(FormatUtils.slug(s.getDescription()));
                }
            }
        }

        List<String> recSlugs = new ArrayList<>();
        if (disease.getRecomendations() != null) {
            for (Recomendation r : disease.getRecomendations()) {
                if (r != null) {
                    recSlugs.add(FormatUtils.slug(r.getDescription()));
                }
            }
        }

        String fact = String.format(
                "assertz(enfermedad(%d, '%s', %s, '%s', %s))",
                disease.getId(),
                FormatUtils.slug(disease.getName()),
                buildPrologList(symptomSlugs),
                FormatUtils.slug(disease.getCategory().getName()),
                buildPrologList(recSlugs)
        );
        System.out.println("[Prolog] Hecho enfermedad -> " + fact);
        PrologQueryExecutor.createDynamicFact(fact);
    }

    private String extractIds(List<? extends Object> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        List<String> ids = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Symptom s) {
                ids.add(String.valueOf(s.getId()));
            } else if (item instanceof Recomendation r) {
                ids.add(String.valueOf(r.getId()));
            }
        }
        return ids.toString();
    }
}
