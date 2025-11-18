package com.example.model;

public class Nota {
    private int id;
    
    // --- CAMPOS CORRIGIDOS (para corresponder ao diagrama) ---
    private int idMatricula; 
    private int idDisciplina; 

    private double valor;
    private String avaliacao;

    // --- CAMPOS EXTRA (para a visualização na tabela) ---
    private String nomeDisciplina; 
    private String nomeAluno;

    /**
     * Construtor atualizado para os campos corretos.
     */
    public Nota(int idMatricula, int idDisciplina, double valor, String avaliacao) {
        this.idMatricula = idMatricula;
        this.idDisciplina = idDisciplina;
        this.valor = valor;
        this.avaliacao = avaliacao;
    }

    // --- Getters e Setters ---
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdMatricula() { return idMatricula; }
    public void setIdMatricula(int idMatricula) { this.idMatricula = idMatricula; }

    public int getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(int idDisciplina) { this.idDisciplina = idDisciplina; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getAvaliacao() { return avaliacao; }
    public void setAvaliacao(String avaliacao) { this.avaliacao = avaliacao; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }
    
    @Override
    public String toString() {
        return "Matrícula ID " + idMatricula + " / Disciplina ID " + idDisciplina + " (" + avaliacao + "): " + valor;
    }
}