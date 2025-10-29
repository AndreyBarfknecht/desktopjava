package com.example.model;

public class Disciplina {
    private int id;
    private String nomeDisciplina;
    private int cargaHoraria;

    // Construtor usado para salvar uma nova disciplina
    public Disciplina(String nomeDisciplina, int cargaHoraria) {
        this.nomeDisciplina = nomeDisciplina;
        this.cargaHoraria = cargaHoraria;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public int getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(int cargaHoraria) { this.cargaHoraria = cargaHoraria; }
   
    /**
     * Override do toString para ser bem exibido em ComboBoxes
     */
    @Override
    public String toString() {
        return this.nomeDisciplina;
    }
}