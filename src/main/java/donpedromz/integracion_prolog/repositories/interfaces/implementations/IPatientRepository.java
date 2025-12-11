package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.repositories.interfaces.IRepository;
import java.util.List;
/**
 * 
 * @author juanp
 */
public interface IPatientRepository extends IRepository<Patient> {
    List<Patient> findByName(String name);
}
