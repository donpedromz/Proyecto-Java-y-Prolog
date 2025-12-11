/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.repositories;

import donpedromz.integracion_prolog.entities.Patient;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IPatientRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import donpedromz.integracion_prolog.shared.MySQLConnection;

/**
 *
 * @author juanp
 */
public class PatientRepository implements IPatientRepository {
    private Connection connection;
    public PatientRepository(){
        this.connection = MySQLConnection.getInstance().getConnection();
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM patient WHERE id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting patient", e);
        }
    }

    @Override
    public List<Patient> getAll() {
        String sql = "SELECT * FROM patient";
        List<Patient> patientList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ps = this.connection.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()){
                patientList.add(this.mapEntity(rs));
            }
        }catch(SQLException e){
            throw new RuntimeException("Error fetching patients", e);
        }finally{
            if(rs != null){
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PatientRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(ps != null){
                try {
                    ps.close();
                } catch (SQLException ex) {
                    Logger.getLogger(PatientRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return patientList;
    }

    @Override
    public Patient getById(long id) {
        String sql = "SELECT * FROM patient WHERE id = ?";
        Patient paciente;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            ps = this.connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if(rs.next()){
                paciente = mapEntity(rs);
                return paciente;
            }else{
                return null;
            }
        }catch(SQLException e){
            throw new RuntimeException("Error fetching patient by id", e);
        }
    }

    @Override
    public Patient update(Patient updateDTO) {
        String sql = "UPDATE patient SET name = ?, age = ? WHERE id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, updateDTO.getName());
            ps.setInt(2, updateDTO.getAge());
            ps.setLong(3, updateDTO.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating patient", e);
        }
        return updateDTO;
    }

    @Override
    public Patient mapEntity(ResultSet set) throws SQLException{
        return new Patient(
                set.getLong("id"),
                set.getString("name"),
                set.getInt("age")
        );
    }
    
    @Override
    public List<Patient> findByName(String name) {
        String sql = "SELECT * FROM patient WHERE name LIKE ?";
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching patients by name", e);
        }
        return patients;
    }
    
}
