package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.repositories.interfaces.IRepository;
import java.util.List;
/**
 * 
 * @author juanp
 */
public interface IDiseaseRepository extends IRepository<Disease> {
    Disease saveDisease(Disease disease);
    List<Symptom> listSymptomsByDiseaseId(long diseaseId);
    List<Recomendation> listRecommendationsByDiseaseId(long diseaseId);
    Disease loadWithRelations(Disease disease);
    List<Disease> getAllWithRelations();
    Disease getByName(String name);
}
