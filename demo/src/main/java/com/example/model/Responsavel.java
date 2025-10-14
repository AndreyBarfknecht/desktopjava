package com.example.model;

public class Responsavel {
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    // outros campos como cpf, telefone...

    public Responsavel(String nomeCompleto, String cpf, String telefone) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    @Override
    public String toString() {
        return this.nomeCompleto;
    }
}