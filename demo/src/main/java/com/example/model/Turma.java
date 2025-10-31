package com.example.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Turma {
    private int id;
    private String nome;
    // --- CAMPOS ALTERADOS ---
    private Curso curso;
    private PeriodoLetivo periodoLetivo;
    private String turno;
    private String sala;

    // --- LISTA DE ALUNOS ---
    private final ObservableList<Aluno> alunosMatriculados = FXCollections.observableArrayList();

    // Construtor principal
    public Turma(String nome, Curso curso, PeriodoLetivo periodoLetivo, String turno, String sala) {
        this.nome = nome;
        this.curso = curso;
        this.periodoLetivo = periodoLetivo;
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

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    // --- GETTER E SETTER PARA O CAMPO SALA ---
    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    // --- GETTERS E SETTERS PARA OS NOVOS CAMPOS ---
    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public PeriodoLetivo getPeriodoLetivo() {
        return periodoLetivo;
    }

    public void setPeriodoLetivo(PeriodoLetivo periodoLetivo) {
        this.periodoLetivo = periodoLetivo;
    }

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

    @Override
    public String toString() {
        return this.nome; // Simplificado para exibição em ComboBoxes
    }
}