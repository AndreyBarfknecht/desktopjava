package com.example.model;

public class Nota {
    private String disciplina;
    private double valor;
    private String avaliacao; // Ex: "Prova Trimestral", "Trabalho"

    public Nota(String disciplina, double valor, String avaliacao) {
        this.disciplina = disciplina;
        this.valor = valor;
        this.avaliacao = avaliacao;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public double getValor() {
        return valor;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    @Override
    public String toString() {
        // Formato que aparecerá em listas, se precisarmos
        return disciplina + " (" + avaliacao + "): " + valor;
    }
}