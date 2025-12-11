/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.ui.modelEntities;

import donpedromz.integracion_prolog.entities.Symptom;

/**
 *
 * @author juanp
 */
public class SymptomModel extends Symptom{
    private Boolean selected;
    public SymptomModel(int id, String description) {
        super(id, description);
    }
    public SymptomModel(Symptom symptom){
        super(symptom.getId(), symptom.getDescription());
        this.selected = false;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    public Symptom toSymptom(SymptomModel model){
        return new Symptom(model.getId(),
                model.getDescription()
        );
    }
}
