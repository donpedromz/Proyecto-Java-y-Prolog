/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.shared;

import donpedromz.integracion_prolog.entities.Disease;
import donpedromz.integracion_prolog.repositories.interfaces.implementations.IDiseaseRepository;
import java.util.List;

/**
 *
 * @author juanp
 */
public class FactsUtils {
    private static FactsUtils instance;
    private IDiseaseRepository diseaseRepository;
    
    public void InitializeFacts(){
        List<Disease> diseases = this.diseaseRepository.getAllWithRelations();
        for(Disease d : diseases){
            StringBuilder sb = new StringBuilder();
            sb.append("assertz(enfermedad(")
                    .append(d.getId()).append(",")
                    .append(FormatUtils.slug(d.getName())).append(",")
                    .append(FormatUtils.toSlugList(d.getSymptoms())).append(",")
                    .append(FormatUtils.slug(d.getCategory().getName())).append(",")
                    .append(FormatUtils.toSlugListFromRecommendations(d.getRecomendations())).append(")).");
            PrologQueryExecutor.createDynamicFact(sb.toString());
        }
    }
    public static FactsUtils getInstance(){
        if(instance == null){
            instance = new FactsUtils();
        }
        return instance;
    }
    public void setDiseaseRepository(IDiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }
}
