/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author juanp
 */
public class MySQLConnection {
    private static MySQLConnection instance;
    private Connection connection;
    /**
     * Credenciales de la DB
     */
    private final String url = "jdbc:mysql://localhost:3307/health_db";
    private final String user = "app_user";
    private final String password = "app_password";
    private MySQLConnection(){
        try{
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connection established with MySQL");
        }catch(SQLException e){
            throw new RuntimeException("Connection cannot be established");
        }
    }
    public static MySQLConnection getInstance(){
        if(instance == null){
            instance = new MySQLConnection();
        }
        return instance;
    }
    public Connection getConnection(){
        return connection;
    }
}
