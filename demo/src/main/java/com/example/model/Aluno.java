package com.example.model;

import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Aluno {
    private int id;
    private String nomeCompleto;
    private String cpf;
    private LocalDate dataNascimento;
    private Responsavel responsavel;
    private String telefone;
    private String email;
    private final ObservableList<Nota> notas = FXCollections.observableArrayList();

    // Construtor principal
    public Aluno(String nomeCompleto, String cpf, LocalDate dataNascimento, Responsavel responsavel, String telefone, String email) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.responsavel = responsavel;
        this.telefone = telefone;
        this.email = email;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public Responsavel getResponsavel() { return responsavel; }
    public void setResponsavel(Responsavel responsavel) { this.responsavel = responsavel; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Métodos para notas
    public ObservableList<Nota> getNotas() {
        return notas;
    }

    public void adicionarNota(Nota nota) {
        this.notas.add(nota);
    }

    @Override
    public String toString() {
        return this.nomeCompleto;
    }

    public String getTelefoneResponsavel() {
    return responsavel != null ? responsavel.getTelefone() : "";
    }
}