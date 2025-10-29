package com.example.model;

// --- NOVAS IMPORTAÇÕES ---
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Turma {
    private int id;
    private String nome;
    private String anoLetivo;
    private String turno;
    private String sala; // NOVO CAMPO

    // --- NOVA LISTA ---
    private final ObservableList<Aluno> alunosMatriculados = FXCollections.observableArrayList();

    // Construtor principal
    public Turma(String nome, String anoLetivo, String turno, String sala) {
        this.nome = nome;
        this.anoLetivo = anoLetivo;
        this.turno = turno;
        this.sala = sala;
    }

    // Construtor vazio para uso em DAOs
    public Turma() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnoLetivo() {
        return anoLetivo;
    }

    public void setAnoLetivo(String anoLetivo) {
        this.anoLetivo = anoLetivo;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    // --- GETTER E SETTER PARA O NOVO CAMPO ---
    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
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
        return this.nome; // Simplificado para exibição em ComboBoxes
    }

    public void setAnoLetivo(int ano) {
        this.anoLetivo = String.valueOf(ano);
    }

   
}