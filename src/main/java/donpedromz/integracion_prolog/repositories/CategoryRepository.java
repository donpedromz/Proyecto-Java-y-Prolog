/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Category;
import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.ICategoryRepository;
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
 */
public class CategoryRepository implements ICategoryRepository {

    private final Connection connection;

    public CategoryRepository() {
        this.connection = MySQLConnection.getInstance().getConnection();
    }

    @Override
    public List<Category> getAll() {
        String sql = "SELECT id, name FROM category";
        List<Category> categories = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(mapEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching categories", e);
        }
        return categories;
    }

    @Override
    public Category getById(long id) {
        String sql = "SELECT id, name FROM category WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching category by id", e);
        }
        return null;
    }

    @Override
    public Category getByName(String name) {
        String sql = "SELECT id, name FROM category WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching category by name", e);
        }
        return null;
    }

    @Override
    public Category update(Category updateDTO) {
        String sql = "UPDATE category SET name = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, updateDTO.getName());
            ps.setLong(2, updateDTO.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
        return updateDTO;
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO category (name) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
            }
            return category;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving category", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM category WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    @Override
    public Category mapEntity(ResultSet set) throws SQLException {
        return new Category(
                set.getInt("id"),
                set.getString("name")
        );
    }

    @Override
    public List<Disease> listDiseasesByCategoryName(String categoryName) {
        String sql = "SELECT d.id, d.name, c.id as category_id, c.name as category_name " +
                     "FROM disease d JOIN category c ON d.category_id = c.id WHERE c.name = ?";
        List<Disease> diseases = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    diseases.add(new Disease(
                        rs.getInt("id"),
                        rs.getString("name"),
                        new Category(rs.getInt("category_id"), rs.getString("category_name")),
                        new ArrayList<>(),
                        new ArrayList<>()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching diseases by category", e);
        }
        return diseases;
    }
}
