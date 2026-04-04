package com.cms.model.enums;

public enum SemesterPeriod {
    S1("1"),
    S2("2");

    private final String value;

    SemesterPeriod(String value) { this.value = value; }

    public String getValue() { return value; }

    public static SemesterPeriod fromValue(String v) {
        for (SemesterPeriod s : values()) {
            if (s.value.equals(v)) return s;
        }
        throw new IllegalArgumentException("Invalid semester: " + v);
    }

    @Override
    public String toString() { return value; }
}
