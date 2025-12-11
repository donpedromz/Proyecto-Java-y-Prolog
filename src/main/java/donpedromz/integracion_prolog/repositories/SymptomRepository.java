/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Symptom;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ISymptomRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import donpedromz.integracion_prolog.shared.MySQLConnection;
import donpedromz.integracion_prolog.shared.FormatUtils;

/**
 *
 * @author juanp
 */
public class SymptomRepository implements ISymptomRepository {

    private final Connection connection;

    public SymptomRepository() {
        this.connection = MySQLConnection.getInstance().getConnection();
    }

    @Override
    public List<Symptom> getAll() {
        String sql = "SELECT id, description FROM symptom";
        List<Symptom> symptoms = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                symptoms.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching symptoms", e);
        }
        return symptoms;
    }

    @Override
    public Symptom getById(long id) {
        String sql = "SELECT id, description FROM symptom WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching symptom by id", e);
        }
        return null;
    }

    @Override
    public Symptom update(Symptom updateDTO) {
        String sql = "UPDATE symptom SET description = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, updateDTO.getDescription());
            ps.setLong(2, updateDTO.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating symptom", e);
        }
        return updateDTO;
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM symptom WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting symptom", e);
        }
    }

    @Override
    public Symptom mapEntity(ResultSet set) throws SQLException {
        return new Symptom(
                set.getInt("id"),
                set.getString("description")
        );
    }

    @Override
    public List<Symptom> listByDiseaseId(long diseaseId) {
        String sql = "SELECT s.id, s.description FROM symptom s " +
                     "JOIN disease_symptom ds ON ds.symptom_id = s.id " +
                     "WHERE ds.disease_id = ?";
        List<Symptom> symptoms = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, diseaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    symptoms.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing symptoms by disease", e);
        }
        return symptoms;
    }

    @Override
    public void associateWithDisease(long diseaseId, List<Symptom> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) return;
        String sql = "INSERT INTO disease_symptom (disease_id, symptom_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Symptom s : symptoms) {
                ps.setLong(1, diseaseId);
                ps.setLong(2, s.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error asociando sintomas a enfermedad", e);
        }
    }

    @Override
    public void saveAll(List<Symptom> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) return;
        for (Symptom symptom : symptoms) {
            save(symptom);
        }
    }

    @Override
    public Symptom getByDescription(String description) {
        String sql = "SELECT id, description FROM symptom WHERE LOWER(description) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching symptom by description", e);
        }
        return null;
    }

    @Override
    public Symptom save(Symptom symptom) {
        if (symptom == null) {
            return null;
        }
        String sql = "INSERT INTO symptom (code, description) VALUES (?, ?)";
        String code = FormatUtils.slug(symptom.getDescription());
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, symptom.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    symptom.setId(rs.getInt(1));
                }
            }
            return symptom;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving symptom", e);
        }
    }
}
