/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package donpedromz.integracion_prolog.entities;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author juanp
 */
public class Patient {
    private long id;
    private String name;
    private int age;
    private List<Diagnostic> diagnostics;

    public Patient(long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.diagnostics = new ArrayList<>();
    }
    
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public void addDiagnostic(Diagnostic diagnostic) {
        this.diagnostics.add(diagnostic);
    }
}
