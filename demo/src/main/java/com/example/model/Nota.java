package com.example.model;

public class Nota {
    private int id;
    
    // Campos ATUALIZADOS para corresponder à tabela 'notas'
    private int idMatricula; 
    private int idDisciplina; 

    private double valor;
    private String avaliacao;

    /**
     * Construtor atualizado para o novo esquema da BD.
     */
    public Nota(int idMatricula, int idDisciplina, double valor, String avaliacao) {
        this.idMatricula = idMatricula;
        this.idDisciplina = idDisciplina;
        this.valor = valor;
        this.avaliacao = avaliacao;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Getters e Setters para os novos campos
    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }
    public int getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(int idDisciplina) { this.idDisciplina = idDisciplina; }

    // Getters e Setters para os campos restantes
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public String getAvaliacao() { return avaliacao; }
    public void setAvaliacao(String avaliacao) { this.avaliacao = avaliacao; }

    @Override
    public String toString() {
        return "Matrícula ID " + idMatricula + " / Disciplina ID " + idDisciplina + " (" + avaliacao + "): " + valor;
    }
}