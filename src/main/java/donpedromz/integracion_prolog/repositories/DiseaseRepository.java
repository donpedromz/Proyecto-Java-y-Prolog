/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Recomendation;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiseaseRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ISymptomRepository;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IRecomendationRepository;
import donpedromz.integracion_prolog.entities.Symptom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import donpedromz.integracion_prolog.shared.MySQLConnection;
/**
 * 
 * @author juanp
 * @author milomnz
 */
public class DiseaseRepository implements IDiseaseRepository {

	private final Connection connection;
    private final ISymptomRepository symptomRepository;
    private final IRecomendationRepository recommendationRepository;

	public DiseaseRepository() {
		this.connection = MySQLConnection.getInstance().getConnection();
        this.symptomRepository = new SymptomRepository();
        this.recommendationRepository = new RecomendationRepository();
	}

    @Override
    public Disease saveDisease(Disease disease) {
        if (disease == null) return null;
        
        try {
            connection.setAutoCommit(false);
            
            // Insertamos la enfermedad y obtenemos el ID
            long diseaseId = insertDisease(disease);
            disease.setId((int) diseaseId); 

            if (disease.getSymptoms() != null && !disease.getSymptoms().isEmpty()) {
                symptomRepository.associateWithDisease(diseaseId, disease.getSymptoms());
            }
			
            if (disease.getRecomendations() != null && !disease.getRecomendations().isEmpty()) {
                recommendationRepository.associateWithDisease(diseaseId, disease.getRecomendations());
            }

            connection.commit();
            return disease;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Error al guardar enfermedad: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    private long insertDisease(Disease disease) throws SQLException {
        String sql = "INSERT INTO disease (name, category_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, disease.getName());
            ps.setInt(2, disease.getCategory().getId());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID para la nueva enfermedad.");
    }
        
        
	@Override
	public List<Disease> getAll() {
		String sql = "SELECT d.id, d.name, c.id as category_id, c.name as category_name " +
				 "FROM disease d JOIN category c ON d.category_id = c.id";
		List<Disease> diseases = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				diseases.add(mapEntity(rs));
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching diseases", e);
		}
		return diseases;
	}

	@Override
	public Disease getById(long id) {
		String sql = "SELECT d.id, d.name, c.id as category_id, c.name as category_name " +
				 "FROM disease d JOIN category c ON d.category_id = c.id WHERE d.id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return loadWithRelations(mapEntity(rs));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching disease by id", e);
		}
		return null;
	}

	@Override
	public Disease getByName(String name) {
		String sql = "SELECT d.id, d.name, c.id as category_id, c.name as category_name " +
				 "FROM disease d JOIN category c ON d.category_id = c.id WHERE LOWER(d.name) = LOWER(?)";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return loadWithRelations(mapEntity(rs));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error fetching disease by name", e);
		}
		return null;
	}

	@Override
	public Disease update(Disease updateDTO) {
		String sql = "UPDATE disease SET name = ?, category_id = ? WHERE id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, updateDTO.getName());
			ps.setInt(2, updateDTO.getCategory().getId());
			ps.setLong(3, updateDTO.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error updating disease", e);
		}
		return updateDTO;
	}

	@Override
	public void deleteById(long id) {
		String sql = "DELETE FROM disease WHERE id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Error deleting disease", e);
		}
	}

	@Override
	public Disease mapEntity(ResultSet set) throws SQLException {
		return new Disease(
				set.getInt("id"),
				set.getString("name"),
				new Category(set.getInt("category_id"), set.getString("category_name")),
				new ArrayList<>(),
				new ArrayList<>()
		);
	}

	@Override
	public List<Symptom> listSymptomsByDiseaseId(long diseaseId) {
		String sql = "SELECT s.id, s.code, s.description FROM symptom s " +
				 "JOIN disease_symptom ds ON ds.symptom_id = s.id " +
				 "WHERE ds.disease_id = ?";
		List<Symptom> symptoms = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, diseaseId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					symptoms.add(new Symptom(
							rs.getInt("id"),
							rs.getString("description")
					)
				);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing symptoms by disease", e);
		}
		return symptoms;
	}

	@Override
	public List<Recomendation> listRecommendationsByDiseaseId(long diseaseId) {
		String sql = "SELECT r.id, r.description FROM recommendation r " +
				 "JOIN disease_recommendation dr ON dr.recommendation_id = r.id " +
				 "WHERE dr.disease_id = ?";
		List<Recomendation> recommendations = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setLong(1, diseaseId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					recommendations.add(new Recomendation(
							rs.getLong("id"),
							rs.getString("description"))
					);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error listing recommendations by disease", e);
		}
		return recommendations;
	}

	@Override
	public Disease loadWithRelations(Disease disease) {
		List<Symptom> symptoms = listSymptomsByDiseaseId(disease.getId());
		List<Recomendation> recs = listRecommendationsByDiseaseId(disease.getId());
		disease.setSymptoms(symptoms);
		disease.setRecomendations(recs);
		return disease;
	}

	@Override
	public List<Disease> getAllWithRelations() {
		List<Disease> bases = getAll();
		List<Disease> full = new ArrayList<>();
		for (Disease d : bases) {
			full.add(loadWithRelations(d));
		}
		return full;
	}
}
