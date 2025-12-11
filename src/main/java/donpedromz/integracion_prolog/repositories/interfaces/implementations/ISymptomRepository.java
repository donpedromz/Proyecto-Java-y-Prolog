package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.repositories.interfaces.IRepository;
import java.util.List;

/**
 * 
 * @author juanp
 */
public interface ISymptomRepository extends IRepository<Symptom> {
    List<Symptom> listByDiseaseId(long diseaseId);
    void associateWithDisease(long diseaseId, List<Symptom> symptoms);
    void saveAll(List<Symptom> symptoms);
}
