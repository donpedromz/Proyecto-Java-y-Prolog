/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.entities;

/**
 *
 * @author juanp
 */
public class Category {
    private int id;
    private String name;

    /**
     * Constructor con id y nombre.
     */
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor solo con nombre (para crear desde slugs de Prolog).
     */
    public Category(String name) {
        this.id = 0;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
 
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
