package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.repositories.interfaces.IRepository;
import java.util.List;

/**
 * 
 * @author juanp
 */
public interface IRecomendationRepository extends IRepository<Recomendation> {
    List<Recomendation> listByDiseaseId(long diseaseId);
    void associateWithDisease(long diseaseId, List<Recomendation> recommendations);
    void saveAll(List<Recomendation> recommendations);
}
