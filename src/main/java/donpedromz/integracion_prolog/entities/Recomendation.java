/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.entities;

/**
 *
 * @author juanp
 */
public class Recomendation {
    private long id;
    private String description;

    /**
     * Constructor con id y descripción.
     */
    public Recomendation(long id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Constructor solo con descripción (para crear desde slugs de Prolog).
     */
    public Recomendation(String description) {
        this.id = 0;
        this.description = description;
    }
    
    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
}
