/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.entities;

/**
 *
 * @author juanp
 */
public class Symptom {
    private int id;
    private String description;

    /**
     * Constructor con id y descripción.
     */
    public Symptom(int id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Constructor solo con descripción (para crear desde slugs de Prolog).
     */
    public Symptom(String description) {
        this.id = 0;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
