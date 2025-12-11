package donpedromz.integracion_prolog.repositories.interfaces.implementations;

import donpedromz.integracion_prolog.entities.Diagnostic;
import java.util.List;

public interface IDiagnosticRepository {
    void saveAll(List<Diagnostic> diagnostics);
    void saveDiagnostic(Diagnostic diagnostic);
    List<Diagnostic> findAll();
    List<Object[]> topDiseases(int limit);
    List<Object[]> topSymptoms(int limit);
}
