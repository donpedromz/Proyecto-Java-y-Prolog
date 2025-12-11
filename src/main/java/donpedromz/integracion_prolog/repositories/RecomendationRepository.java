/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IRecomendationRepository;
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
public class RecomendationRepository implements IRecomendationRepository {

    private final Connection connection;

    public RecomendationRepository() {
        this.connection = MySQLConnection.getInstance().getConnection();
    }

    @Override
    public List<Recomendation> getAll() {
        String sql = "SELECT id, description FROM recommendation";
        List<Recomendation> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recommendations", e);
        }
        return items;
    }

    @Override
    public Recomendation getById(long id) {
        String sql = "SELECT id, description FROM recommendation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recommendation by id", e);
        }
        return null;
    }

    @Override
    public Recomendation update(Recomendation updateDTO) {
        String sql = "UPDATE recommendation SET description = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, updateDTO.getDescription());
            ps.setLong(2, updateDTO.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating recommendation", e);
        }
        return updateDTO;
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM recommendation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting recommendation", e);
        }
    }

    @Override
    public Recomendation mapEntity(ResultSet set) throws SQLException {
        return new Recomendation(
                set.getLong("id"),
                set.getString("description")
        );
    }

    @Override
    public List<Recomendation> listByDiseaseId(long diseaseId) {
        String sql = "SELECT r.id, r.description FROM recommendation r " +
                     "JOIN disease_recommendation dr ON dr.recommendation_id = r.id " +
                     "WHERE dr.disease_id = ?";
        List<Recomendation> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, diseaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing recommendations by disease", e);
        }
        return items;
    }

    @Override
    public void associateWithDisease(long diseaseId, List<Recomendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) return;
        String sql = "INSERT INTO disease_recommendation (disease_id, recommendation_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Recomendation r : recommendations) {
                ps.setLong(1, diseaseId);
                ps.setLong(2, r.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error asociando recomendaciones a enfermedad", e);
        }
    }

    @Override
    public void saveAll(List<Recomendation> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) return;
        for (Recomendation rec : recommendations) {
            save(rec);
        }
    }

    @Override
    public Recomendation getByDescription(String description) {
        String sql = "SELECT id, description FROM recommendation WHERE LOWER(description) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, description);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recommendation by description", e);
        }
        return null;
    }

    @Override
    public Recomendation save(Recomendation recomendation) {
        if (recomendation == null) {
            return null;
        }
        String sql = "INSERT INTO recommendation (code, description) VALUES (?, ?)";
        String code = FormatUtils.slug(recomendation.getDescription());
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, recomendation.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    recomendation.setId(rs.getLong(1));
                }
            }
            return recomendation;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving recommendation", e);
        }
    }
}
