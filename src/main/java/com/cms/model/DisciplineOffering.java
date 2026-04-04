package com.cms.model;

import com.cms.model.enums.SemesterPeriod;
import com.cms.server.JsonUtil;
import java.time.Instant;
import java.util.UUID;

public class DisciplineOffering {

    private String id;
    private String disciplineId;
    private String professorId;
    private int year;
    private SemesterPeriod semester;
    private String classCode;
    private String createdAt;

    public DisciplineOffering() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = Instant.now().toString();
    }

    public DisciplineOffering(String disciplineId, String professorId, int year, SemesterPeriod semester) {
        this();
        this.disciplineId = disciplineId;
        this.professorId  = professorId;
        this.year         = year;
        this.semester     = semester;
    }

    public String toJson() {
        return "{"
            + "\"id\":"           + q(id)           + ","
            + "\"disciplineId\":" + q(disciplineId)  + ","
            + "\"professorId\":"  + q(professorId)   + ","
            + "\"year\":"         + year             + ","
            + "\"semester\":"     + q(semester)      + ","
            + "\"classCode\":"    + q(classCode)     + ","
            + "\"createdAt\":"    + q(createdAt)
            + "}";
    }

    private static String q(Object v) {
        if (v == null) return "null";
        return "\"" + JsonUtil.escapeJson(v.toString()) + "\"";
    }

    // Getters and Setters
    public String getId()                              { return id; }
    public void setId(String id)                       { this.id = id; }

    public String getDisciplineId()                    { return disciplineId; }
    public void setDisciplineId(String disciplineId)   { this.disciplineId = disciplineId; }

    public String getProfessorId()                     { return professorId; }
    public void setProfessorId(String professorId)     { this.professorId = professorId; }

    public int getYear()                               { return year; }
    public void setYear(int year)                      { this.year = year; }

    public SemesterPeriod getSemester()                { return semester; }
    public void setSemester(SemesterPeriod semester)   { this.semester = semester; }

    public String getClassCode()                       { return classCode; }
    public void setClassCode(String classCode)         { this.classCode = classCode; }

    public String getCreatedAt()                       { return createdAt; }
}
