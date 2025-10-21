package com.example.model;

public class Responsavel {
    private int id; // NOVO: Campo para o ID da base de dados
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    private String email; // NOVO: Campo para o email

    // Construtor atualizado para incluir o email
    public Responsavel(String nomeCompleto, String cpf, String telefone, String email) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
    }

    // Getters e Setters para todos os campos, incluindo o novo ID
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return this.nomeCompleto;
    }
}