package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiagnosticRepository;
import donpedromz.integracion_prolog.shared.MySQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class DiagnosticRepository implements IDiagnosticRepository {

    private static final String INSERT_DIAGNOSTIC = "INSERT INTO diagnostic (patient_name, patient_age, disease_id, disease_name, category, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_DIAGNOSTIC_SYMPTOM = "INSERT INTO diagnostic_symptom (diagnostic_id, symptom_description) VALUES (?, ?)";
    private static final String INSERT_DIAGNOSTIC_REC = "INSERT INTO diagnostic_recomendation (diagnostic_id, recomendation_description) VALUES (?, ?)";

    @Override
    public void saveAll(List<Diagnostic> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) return;
        for (Diagnostic d : diagnostics) {
            saveDiagnostic(d);
        }
    }

    @Override
    public void saveDiagnostic(Diagnostic diagnostic) {
        if (diagnostic == null) return;
        Connection conn = MySQLConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            long diagId = insertDiagnostic(conn, diagnostic);
            insertSymptoms(conn, diagId, diagnostic.getDiseaseSymptoms());
            insertRecs(conn, diagId, diagnostic.getRecommendations());
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Error al guardar diagnostico", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private long insertDiagnostic(Connection conn, Diagnostic d) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DIAGNOSTIC, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getPatient() != null ? d.getPatient().getName() : null);
            ps.setInt(2, d.getPatient() != null ? d.getPatient().getAge() : 0);
            ps.setInt(3, d.getDisease() != null ? d.getDisease().getId() : 0);
            ps.setString(4, d.getDisease() != null ? d.getDisease().getName() : null);
            ps.setString(5, d.getCategoryName());
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    private void insertSymptoms(Connection conn, long diagId, List<Symptom> symptoms) throws SQLException {
        if (symptoms == null) return;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DIAGNOSTIC_SYMPTOM)) {
            for (Symptom s : symptoms) {
                ps.setLong(1, diagId);
                ps.setString(2, s != null ? s.getDescription() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertRecs(Connection conn, long diagId, List<Recomendation> recs) throws SQLException {
        if (recs == null) return;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DIAGNOSTIC_REC)) {
            for (Recomendation r : recs) {
                ps.setLong(1, diagId);
                ps.setString(2, r != null ? r.getDescription() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
