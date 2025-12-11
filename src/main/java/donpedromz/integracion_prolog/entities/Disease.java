/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.entities;

import java.util.List;

/**
 *
 * @author juanp
 */
public class Disease {
    private int id;
    private String name;
    private Category category;
    private List<Symptom> symptoms;
    private List<Recomendation> recomendations;
    public Disease(){};
    public Disease(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Disease(int id, String name, Category category, List<Symptom> symptoms, List<Recomendation> recomendations) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.symptoms = symptoms;
        this.recomendations = recomendations;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public List<Recomendation> getRecomendations() {
        return recomendations;
    }

    public void setRecomendations(List<Recomendation> recomendations) {
        this.recomendations = recomendations;
    }
    
}
