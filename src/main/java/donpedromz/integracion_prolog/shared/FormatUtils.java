package donpedromz.integracion_prolog.shared;

import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jpl7.Term;

/**
 * 
 * @author juanp
 * @author milomnz
 */
public final class FormatUtils {
    private FormatUtils() {}

    /**
     * Convierte las descripciones de síntomas a minúsculas y con separadores "_".
     */
    public static List<String> toSlugList(List<Symptom> symptoms) {
        if (symptoms == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Symptom s : symptoms) {
            if (s == null || s.getDescription() == null) continue;
            out.add(slug(s.getDescription()));
        }
        return out;
    }

    /**
     * Convierte las descripciones de recomendaciones a minúsculas y con separadores "_".
     */
    public static List<String> toSlugListFromRecommendations(List<Recomendation> recomendations) {
        if (recomendations == null) return new ArrayList<>();
        List<String> out = new ArrayList<>();
        for (Recomendation r : recomendations) {
            if (r == null || r.getDescription() == null) continue;
            out.add(slug(r.getDescription()));
        }
        return out;
    }

    /**
     * Normaliza una cadena para usarla como átomo en Prolog: minúsculas, espacios y guiones a "_".
     * Ejemplo: "COVID-19" -> "covid_19"; "Dolor Cabeza" -> "dolor_cabeza".
     */
    public static String slug(String text) {
        if (text == null) return null;
        // Normaliza a ASCII para evitar caracteres con acentos que rompen los átomos de Prolog
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        // Sustituye separadores comunes por guion bajo
        String slug = normalized.toLowerCase(Locale.ROOT)
            .replace(' ', '_')
            .replace('-', '_');
        // Elimina cualquier carácter no alfanumérico o guion bajo
        slug = slug.replaceAll("[^a-z0-9_]", "_");
        // Colapsa múltiples guiones bajos contiguos
        while (slug.contains("__")) {
            slug = slug.replace("__", "_");
        }
        return slug.trim();
    }

    /**
     * Convierte un slug de Prolog a texto presentable para la UI.
     * Ejemplo: "dolor_de_cabeza" -> "Dolor De Cabeza"
     * @param slug El slug proveniente de Prolog
     * @return Texto con espacios y cada palabra capitalizada
     */
    public static String unslug(String slug) {
        if (slug == null || slug.isEmpty()) return slug;
        String[] words = slug.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                sb.append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    /**
     * Convierte una lista de slugs a una lista de Symptom con descripción presentable.
     */
    public static List<Symptom> slugsToSymptoms(List<String> slugs) {
        List<Symptom> symptoms = new ArrayList<>();
        for (String slug : slugs) {
            symptoms.add(new Symptom(unslug(slug)));
        }
        return symptoms;
    }

    /**
     * Convierte una lista de slugs a una lista de Recomendation con descripción presentable.
     */
    public static List<Recomendation> slugsToRecomendations(List<String> slugs) {
        List<Recomendation> recs = new ArrayList<>();
        for (String slug : slugs) {
            recs.add(new Recomendation(unslug(slug)));
        }
        return recs;
    }

    /**
     * Crea una Category a partir de un slug.
     */
    public static Category slugToCategory(String slug) {
        return new Category(unslug(slug));
    }

    /**
     * Convierte un Term de lista Prolog a una lista Java de Strings.
     * @param listTerm El término Prolog que representa una lista
     * @return Lista de Strings con los elementos
     */
    public static List<String> prologListToJavaList(Term listTerm) {
        List<String> result = new ArrayList<>();

        if (listTerm == null) {
            return result;
        }

        try {
            Term cursor = listTerm;
            while (cursor.isListPair()) {
                Term head = cursor.arg(1);
                result.add(head.name());
                cursor = cursor.arg(2);
            }
            if (cursor.isListNil()) {
                return result;
            }
            result.add(cursor.toString());
        } catch (Exception e) {
            // Si falla la conversión, intentamos obtenerlo como átomo simple
            result.add(listTerm.toString());
        }
        return result;
    }
}
