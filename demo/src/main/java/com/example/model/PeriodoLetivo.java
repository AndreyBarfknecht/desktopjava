package com.example.model;

import java.time.LocalDate;

public class PeriodoLetivo {
    private int id;
    private String nome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String status;

    // Construtor usado pelo formulário
    public PeriodoLetivo(String nome, LocalDate dataInicio, LocalDate dataFim, String status) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
    }
    
    // Construtor usado pelo DAO (para carregar do banco)
    public PeriodoLetivo(int id, String nome, LocalDate dataInicio, LocalDate dataFim, String status) {
        this(nome, dataInicio, dataFim, status);
        this.id = id;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return this.nome;
    }
}