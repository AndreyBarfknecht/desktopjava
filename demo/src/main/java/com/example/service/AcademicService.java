package com.example.service;

import com.example.model.Aluno;
import com.example.model.PeriodoLetivo;
import com.example.model.Professor;
import com.example.model.Turma;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Esta classe funciona como um armazém de dados em memória.
 * O padrão Singleton garante que teremos apenas UMA instância desta classe
 * em toda a aplicação.
 */
public class AcademicService {

    private static AcademicService instance;

    private final ObservableList<Professor> professores = FXCollections.observableArrayList();
    private final ObservableList<Turma> turmas = FXCollections.observableArrayList();
    private final ObservableList<PeriodoLetivo> periodosLetivos = FXCollections.observableArrayList();
    private final ObservableList<Aluno> alunos = FXCollections.observableArrayList();
    
    private AcademicService() {
        // Construtor privado para evitar que seja instanciado externamente
    }

    public static AcademicService getInstance() {
        if (instance == null) {
            instance = new AcademicService();
        }
        return instance;
    }

    // Métodos para Professores
    public void addProfessor(Professor professor) {
        professores.add(professor);
    }

    public ObservableList<Professor> getProfessores() {
        return professores;
    }

    // Métodos para Turmas
    public void addTurma(Turma turma) {
        turmas.add(turma);
    }

    public ObservableList<Turma> getTurmas() {
        return turmas;
    }

    // Métodos para Períodos Letivos
    public void addPeriodoLetivo(PeriodoLetivo periodo) {
        periodosLetivos.add(periodo);
    }

    public ObservableList<PeriodoLetivo> getPeriodosLetivos() {
        return periodosLetivos;
    }
    
    public void addAluno(Aluno aluno) {
        alunos.add(aluno);
    }

    public ObservableList<Aluno> getAlunos() {
        return alunos;
    }
}