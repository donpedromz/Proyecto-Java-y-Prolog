package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.repositories.interfaces.IRepository;
import java.util.List;
/**
 * 
 * @author juanp
 */
public interface ICategoryRepository extends IRepository<Category> {
    List<Disease> listDiseasesByCategoryName(String categoryName);
}
