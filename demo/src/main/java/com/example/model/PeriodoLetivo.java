package com.example.model;

public class PeriodoLetivo {
    private String nome;

    public PeriodoLetivo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}