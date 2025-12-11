package donpedromz.integracion_prolog;


import donpedromz.integracion_prolog.controllers.SpecialistController;
import donpedromz.integracion_prolog.repositories.CategoryRepository;
import donpedromz.integracion_prolog.repositories.DiseaseRepository;
import donpedromz.integracion_prolog.repositories.SymptomRepository;
import donpedromz.integracion_prolog.services.SpecialistService;
import org.jpl7.Query;
import donpedromz.integracion_prolog.shared.FactsUtils;
import donpedromz.integracion_prolog.ui.EntryFrame;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author juanp
 */
public class Main {
    private static void initRules(){
        String rules = "consult('prolog.pl')";
        Query q = new Query(rules);
        if(q.hasSolution()){
            System.out.println("Prolog cargado");
        }
    }
    public static void main(String[] args) {
        initRules();
        /**
         * Iniciar Repositorios, servicio y controlador mediante
         * inyección en el constructor
         */
        DiseaseRepository diseaseRepository = new DiseaseRepository();
        SymptomRepository symptomRepository = new SymptomRepository();
        CategoryRepository categoryRepository = new CategoryRepository();
        SpecialistService service = new SpecialistService(categoryRepository, diseaseRepository, symptomRepository);
        SpecialistController controller = new SpecialistController(service);
        /**
         * Inicializar Hechos dinamicos que usará la aplicación.
         */
        FactsUtils utils = FactsUtils.getInstance();
        utils.setDiseaseRepository(diseaseRepository);
        utils.InitializeFacts();
        /**
         * Inicializar la UI, con entrada en el EntryFrame
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EntryFrame frame = new EntryFrame(controller);
                controller.setEntryFrame(frame);
                frame.setVisible(true);
            }
        });
    }
}
