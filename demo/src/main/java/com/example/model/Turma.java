package com.example.model;

// --- NOVAS IMPORTAÇÕES ---
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Turma {
    private String nome;
    private String anoLetivo;
    // --- NOVA LISTA ---
    private final ObservableList<Aluno> alunosMatriculados = FXCollections.observableArrayList();

    public Turma(String nome, String anoLetivo) {
        this.nome = nome;
        this.anoLetivo = anoLetivo;
    }

    public String getNome() {
        return nome;
    }
    
    // --- MÉTODOS NOVOS ---
    public ObservableList<Aluno> getAlunosMatriculados() {
        return alunosMatriculados;
    }

    public void matricular(Aluno aluno) {
        if (!alunosMatriculados.contains(aluno)) {
            alunosMatriculados.add(aluno);
        }
    }

    public void desmatricular(Aluno aluno) {
        alunosMatriculados.remove(aluno);
    }
    // --- FIM DOS MÉTODOS NOVOS ---

    @Override
    public String toString() {
        return this.nome + " (" + this.anoLetivo + ")";
    }
}