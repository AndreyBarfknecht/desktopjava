package com.example.model;

import java.time.LocalDate;

public class Professor {
    private int id; // Corresponderá à chave primária da base de dados
    private String nomeCompleto;
    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String disciplinaPrincipal; // Corresponde à coluna na tabela

    // Construtor usado para criar um professor a partir do formulário
    public Professor(String nomeCompleto, String cpf, LocalDate dataNascimento, String email, String telefone, String disciplinaPrincipal) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.telefone = telefone;
        this.disciplinaPrincipal = disciplinaPrincipal;
    }

    // --- Getters e Setters para todos os campos ---
    // (Boas práticas para permitir o acesso controlado aos dados)

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getDisciplinaPrincipal() { return disciplinaPrincipal; }
    public void setDisciplinaPrincipal(String disciplinaPrincipal) { this.disciplinaPrincipal = disciplinaPrincipal; }

    @Override
    public String toString() {
        return this.nomeCompleto;
    }
}