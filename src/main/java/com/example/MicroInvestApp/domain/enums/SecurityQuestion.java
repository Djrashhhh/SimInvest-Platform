package com.example.MicroInvestApp.domain.enums;

public enum SecurityQuestion {
    MOTHER_MAIDEN_NAME("What is your mother's maiden name?"),
    FIRST_PET_NAME("What was the name of your first pet?"),
    HIGH_SCHOOL_NAME("What is the name of your high school?");



    private final String question;    // The question text

    SecurityQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
