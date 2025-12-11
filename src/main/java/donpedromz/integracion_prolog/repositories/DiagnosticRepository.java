package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Diagnostic;
import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiagnosticRepository;
import donpedromz.integracion_prolog.shared.MySQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

public class DiagnosticRepository implements IDiagnosticRepository {

    private static final String INSERT_DIAGNOSTIC = "INSERT INTO diagnostic (patient_name, patient_age, created_at) VALUES (?, ?, ?)";
    private static final String INSERT_INPUT_SYMPTOM = "INSERT INTO diagnostic_input_symptom (diagnostic_id, symptom_description) VALUES (?, ?)";
    private static final String INSERT_DIAGNOSTIC_DISEASE = "INSERT INTO diagnostic_disease (diagnostic_id, disease_id) VALUES (?, ?)";

    private static final String SELECT_DIAGNOSTICS = "SELECT id, patient_name, patient_age FROM diagnostic";
    private static final String SELECT_INPUT_SYMPTOMS = "SELECT symptom_description FROM diagnostic_input_symptom WHERE diagnostic_id = ?";
    private static final String SELECT_DIAG_DISEASE_IDS = "SELECT disease_id FROM diagnostic_disease WHERE diagnostic_id = ?";

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
            diagnostic.setId((int) diagId);
            insertInputSymptoms(conn, diagId, diagnostic.getInputSymptoms());
            insertDiseases(conn, diagId, diagnostic.getDiseases());
            conn.commit();
            System.out.println("Diagnostico creado en DB con id=" + diagId + ", paciente='" + (diagnostic.getPatient() != null ? diagnostic.getPatient().getName() : "") + "'");
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Error al guardar diagnostico", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    @Override
    public List<Diagnostic> findAll() {
        List<Diagnostic> diagnostics = new ArrayList<>();
        Connection conn = MySQLConnection.getInstance().getConnection();

        try (PreparedStatement ps = conn.prepareStatement(SELECT_DIAGNOSTICS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long diagId = rs.getLong("id");
                String patientName = rs.getString("patient_name");
                int patientAge = rs.getInt("patient_age");

                List<Symptom> inputSymptoms = fetchInputSymptoms(conn, diagId);
                List<Disease> diseases = fetchDiseases(conn, diagId);

                Patient patient = new Patient(0, patientName, patientAge);
                Diagnostic diagnostic = new Diagnostic((int) diagId, patient, diseases, inputSymptoms);
                diagnostics.add(diagnostic);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar diagnosticos", e);
        }

        return diagnostics;
    }

    private long insertDiagnostic(Connection conn, Diagnostic d) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DIAGNOSTIC, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getPatient() != null ? d.getPatient().getName() : null);
            ps.setInt(2, d.getPatient() != null ? d.getPatient().getAge() : 0);
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    private void insertInputSymptoms(Connection conn, long diagId, List<Symptom> symptoms) throws SQLException {
        if (symptoms == null || symptoms.isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_INPUT_SYMPTOM)) {
            for (Symptom s : symptoms) {
                ps.setLong(1, diagId);
                ps.setString(2, s != null ? s.getDescription() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertDiseases(Connection conn, long diagId, List<Disease> diseases) throws SQLException {
        if (diseases == null || diseases.isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DIAGNOSTIC_DISEASE)) {
            for (Disease d : diseases) {
                if (d == null) {
                    continue;
                }
                ps.setLong(1, diagId);
                ps.setInt(2, d.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Symptom> fetchInputSymptoms(Connection conn, long diagnosticId) throws SQLException {
        List<Symptom> symptoms = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_INPUT_SYMPTOMS)) {
            ps.setLong(1, diagnosticId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    symptoms.add(new Symptom(rs.getString("symptom_description")));
                }
            }
        }
        return symptoms;
    }

    private List<Disease> fetchDiseases(Connection conn, long diagnosticId) throws SQLException {
        List<Disease> diseases = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_DIAG_DISEASE_IDS)) {
            ps.setLong(1, diagnosticId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int diseaseId = rs.getInt("disease_id");
                    Disease disease = loadDiseaseAggregate(conn, diseaseId);
                    if (disease != null) {
                        diseases.add(disease);
                    }
                }
            }
        }
        return diseases;
    }

    private Disease loadDiseaseAggregate(Connection conn, int diseaseId) throws SQLException {
        Disease disease = null;
        String sql = "SELECT d.id, d.name, c.name AS category_name FROM disease d LEFT JOIN category c ON d.category_id = c.id WHERE d.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diseaseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String categoryName = rs.getString("category_name");
                    Category category = new Category(categoryName);
                    List<Symptom> symptoms = loadDiseaseSymptoms(conn, diseaseId);
                    List<Recomendation> recs = loadDiseaseRecommendations(conn, diseaseId);
                    disease = new Disease(diseaseId, name, category, symptoms, recs);
                }
            }
        }
        return disease;
    }

    private List<Symptom> loadDiseaseSymptoms(Connection conn, int diseaseId) throws SQLException {
        List<Symptom> symptoms = new ArrayList<>();
        String sql = "SELECT s.description FROM disease_symptom ds JOIN symptom s ON s.id = ds.symptom_id WHERE ds.disease_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diseaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    symptoms.add(new Symptom(rs.getString("description")));
                }
            }
        }
        return symptoms;
    }

    private List<Recomendation> loadDiseaseRecommendations(Connection conn, int diseaseId) throws SQLException {
        List<Recomendation> recs = new ArrayList<>();
        String sql = "SELECT r.description FROM disease_recommendation dr JOIN recommendation r ON r.id = dr.recommendation_id WHERE dr.disease_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diseaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recs.add(new Recomendation(rs.getString("description")));
                }
            }
        }
        return recs;
    }

    @Override
    public List<Object[]> topDiseases(int limit) {
        List<Object[]> items = new ArrayList<>();
        String sql = "SELECT d.name AS label, COUNT(*) AS total "
                + "FROM diagnostic_disease dd "
                + "JOIN disease d ON d.id = dd.disease_id "
                + "GROUP BY d.name "
                + "ORDER BY total DESC "
                + "LIMIT ?";
        Connection conn = MySQLConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Object[]{rs.getString("label"), rs.getLong("total")});
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo enfermedades mas frecuentes", e);
        }
        return items;
    }

    @Override
    public List<Object[]> topSymptoms(int limit) {
        List<Object[]> items = new ArrayList<>();
        String sql = "SELECT symptom_description AS label, COUNT(*) AS total "
                + "FROM diagnostic_input_symptom "
                + "GROUP BY symptom_description "
                + "ORDER BY total DESC "
                + "LIMIT ?";
        Connection conn = MySQLConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Object[]{rs.getString("label"), rs.getLong("total")});
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo sintomas mas frecuentes", e);
        }
        return items;
    }
}
