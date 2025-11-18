package com.example.model;

import java.time.LocalDate;

public class Matricula {
    private int id;
    private Aluno aluno;
    private Turma turma;
    private LocalDate dataMatricula;
    private String status;

    public Matricula(Aluno aluno, Turma turma, LocalDate dataMatricula, String status) {
        this.aluno = aluno;
        this.turma = turma;
        this.dataMatricula = dataMatricula;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public void setDataMatricula(LocalDate dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Métodos para facilitar a exibição na TableView
    public String getNomeAluno() {
        return aluno != null ? aluno.getNomeCompleto() : "";
    }

    public String getNomeTurma() {
        return turma != null ? turma.getNome() : "";
    }
    
    /**
     * Retorna o CPF do aluno associado a esta matrícula.
     * Usado pela TableView (property="cpfAluno").
     */
    public String getCpfAluno() {
        return aluno != null ? aluno.getCpf() : "";
    }

    public String getNomeCurso() {
        return (turma != null && turma.getCurso() != null) 
               ? turma.getCurso().getNomeCurso() : "";
    }
}
