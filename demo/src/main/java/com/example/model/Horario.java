package com.example.model;

public class Horario {
    
    private int id;
    private PeriodoLetivo periodoLetivo;
    private Turma turma;
    private Professor professor;
    private String diaSemana;
    private String horaInicio; 
    private String horaFim;    

    public Horario(PeriodoLetivo periodoLetivo, Turma turma, Professor professor, 
                   String diaSemana, String horaInicio, String horaFim) {
        this.periodoLetivo = periodoLetivo;
        this.turma = turma;
        this.professor = professor;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PeriodoLetivo getPeriodoLetivo() { return periodoLetivo; }
    public void setPeriodoLetivo(PeriodoLetivo periodoLetivo) { this.periodoLetivo = periodoLetivo; }
    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }
}