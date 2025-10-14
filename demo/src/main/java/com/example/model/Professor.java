package com.example.model; // <-- ADICIONE ESTA LINHA

public class Professor {
    private String nomeCompleto;
    private String cpf;
    // Adicione outros campos se desejar

    public Professor(String nomeCompleto, String cpf) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    @Override
    public String toString() {
        // Isso é o que aparecerá na lista de seleção (ComboBox)
        return this.nomeCompleto;
    }
}