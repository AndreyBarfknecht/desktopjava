package com.example.model;

// --- NOVA IMPORTAÇÃO ---
import com.example.model.Disciplina;

public class Horario {
    
    private int id;
    // --- ALTERADO ---
    private Disciplina disciplina; // Substituímos PeriodoLetivo por Disciplina
    private Turma turma;
    private Professor professor;
    private String diaSemana;
    private String horaInicio; 
    private String horaFim;    

    // --- CONSTRUTOR ALTERADO ---
    public Horario(Disciplina disciplina, Turma turma, Professor professor, 
                   String diaSemana, String horaInicio, String horaFim) {
        this.disciplina = disciplina; // Alterado
        this.turma = turma;
        this.professor = professor;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    // --- GETTERS E SETTERS ALTERADOS ---
    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }
    
    // (O resto dos getters/setters para turma, professor, etc., continuam iguais)
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

    // --- MÉTODOS DE CONVENIÊNCIA PARA A TABLEVIEW ---

    public String getNomeTurma() {
        return turma != null ? turma.getNome() : "";
    }

    public String getNomeProfessor() {
        return professor != null ? professor.getNomeCompleto() : "";
    }

    // --- LÓGICA ALTERADA ---
    // O Período Letivo agora é obtido através da Turma
    public String getNomePeriodoLetivo() {
        return (turma != null && turma.getPeriodoLetivo() != null) 
               ? turma.getPeriodoLetivo().getNome() : "";
    }
    
    // --- NOVO MÉTODO (precisamos dele para o FXML) ---
    public String getNomeDisciplina() {
        return disciplina != null ? disciplina.getNomeDisciplina() : "";
    }
}