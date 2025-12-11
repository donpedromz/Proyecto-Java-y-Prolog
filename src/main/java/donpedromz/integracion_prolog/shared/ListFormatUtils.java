package donpedromz.integracion_prolog.shared;

import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author juanp
 */
public final class ListFormatUtils {
    private ListFormatUtils() {}

    /**
     * Convierte las descripciones de síntomas a minúsculas y con espacios reemplazados por guiones bajos.
     */
    public static List<String> toSlugList(List<Symptom> symptoms) {
        if (symptoms.isEmpty()) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Symptom s : symptoms) {
            if (s == null || s.getDescription() == null) continue;
            out.add(slug(s.getDescription()));
        }
        return out;
    }

    /**
     * Convierte las descripciones de recomendaciones a minúsculas y con espacios reemplazados por guiones bajos.
     */
    public static List<String> toSlugListFromRecommendations(List<Recomendation> recomendations) {
        if (recomendations.isEmpty()) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Recomendation r : recomendations) {
            if (r == null || r.getDescription() == null) continue;
            out.add(slug(r.getDescription()));
        }
        return out;
    }
    private static String slug(String text) {
        return text.toLowerCase().replace(" ", "_");
    }
}
