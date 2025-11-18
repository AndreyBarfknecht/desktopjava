package com.example.model;

public class Nota {
    
    // --- CORRIGIDO ---
    private int idNota; // Alterado de 'id' para 'idNota'
    
    private int idMatricula; 
    private int idDisciplina; 
    private double valor;
    private String avaliacao;

    // Campos extra para a UI
    private String nomeDisciplina; 
    private String nomeAluno;

    public Nota(int idMatricula, int idDisciplina, double valor, String avaliacao) {
        this.idMatricula = idMatricula;
        this.idDisciplina = idDisciplina;
        this.valor = valor;
        this.avaliacao = avaliacao;
    }

    // --- GETTER E SETTER CORRIGIDOS ---
    public int getIdNota() { return idNota; }
    public void setIdNota(int idNota) { this.idNota = idNota; }

    // --- Restante dos Getters e Setters ---
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
}