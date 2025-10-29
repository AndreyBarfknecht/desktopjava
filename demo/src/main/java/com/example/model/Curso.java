package com.example.model;

public class Curso {
    private int id;
    private String nomeCurso;
    private String nivel;
    private int duracaoSemestres;

    // Construtor usado para salvar um novo curso
    public Curso(String nomeCurso, String nivel, int duracaoSemestres) {
        this.nomeCurso = nomeCurso;
        this.nivel = nivel;
        this.duracaoSemestres = duracaoSemestres;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeCurso() { return nomeCurso; }
    public void setNomeCurso(String nomeCurso) { this.nomeCurso = nomeCurso; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public int getDuracaoSemestres() { return duracaoSemestres; }
    public void setDuracaoSemestres(int duracaoSemestres) { this.duracaoSemestres = duracaoSemestres; }

    /**
     * Override do toString para ser bem exibido em ComboBoxes
     */
    @Override
    public String toString() {
        return this.nomeCurso;
    }
}