/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;

/**
 * Abstract quiz question. Subclasses decide how the question is shown
 * and how an answer is checked, so a Quiz can hold any mix of question
 * types without knowing which is which (runtime polymorphism).
 */
public abstract class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String text;
    private final int marks;

    protected Question(String text, int marks) {
        this.text = text;
        this.marks = marks;
    }

    public String getText() {
        return text;
    }

    public int getMarks() {
        return marks;
    }

    /** Full text shown to the student, including any options. */
    public abstract String getPrompt();

    /** Short hint describing what may be typed, e.g. "A-D". */
    public abstract String getAnswerHint();

    /** @return true if the text is an acceptable answer format for this question */
    public abstract boolean isValidAnswer(String answer);

    /** @return true if the answer is correct */
    public abstract boolean isCorrect(String answer);

    /** Human-readable correct answer, shown in the review after a quiz. */
    public abstract String getCorrectAnswerText();
}
