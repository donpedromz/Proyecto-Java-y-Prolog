package donpedromz.integracion_prolog.repositories.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
/**
 * 
 * @author juanp
 * @param <T> 
 */
public interface IRepository<T> {
    List<T> getAll();
    T getById(long id);
    T update(T updateDTO);
    void deleteById(long id);
    T mapEntity(ResultSet set) throws SQLException;
}
