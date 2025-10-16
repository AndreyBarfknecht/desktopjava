package com.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Aluno {
    private String nomeCompleto;
    private String cpf;
    private LocalDate dataNascimento;
    private Responsavel responsavel;
    private final List<Nota> notas = new ArrayList<>();

    public Aluno(String nomeCompleto, String cpf, LocalDate dataNascimento, Responsavel responsavel) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.responsavel = responsavel;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }
    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public Responsavel getResponsavel() {
        return responsavel;
    }
    
    public void adicionarNota(Nota nota) {
        this.notas.add(nota);
    }

    public List<Nota> getNotas() {
        return notas;
    }
    
    @Override
    public String toString() {
        return this.nomeCompleto;
    }
    
}