/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A question with 2 to 5 options, answered with a letter (A, B, ...) or a number (1, 2, ...). */
public class MultipleChoiceQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private final List<String> options;
    private final int correctIndex;

    public MultipleChoiceQuestion(String text, int marks, List<String> options, int correctIndex) {
        super(text, marks);
        if (options == null || options.size() < 2 || options.size() > 5) {
            throw new IllegalArgumentException("A multiple-choice question needs 2 to 5 options.");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct option is out of range.");
        }
        this.options = new ArrayList<String>(options);
        this.correctIndex = correctIndex;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /** Converts "B", "b" or "2" to index 1. Returns -1 if the text is not a valid option. */
    private int toIndex(String answer) {
        if (answer == null) {
            return -1;
        }
        String a = answer.trim().toUpperCase();
        if (a.length() != 1) {
            return -1;
        }
        char c = a.charAt(0);
        int index;
        if (c >= 'A' && c <= 'E') {
            index = c - 'A';
        } else if (c >= '1' && c <= '5') {
            index = c - '1';
        } else {
            return -1;
        }
        return index < options.size() ? index : -1;
    }

    @Override
    public String getPrompt() {
        StringBuilder sb = new StringBuilder(getText());
        for (int i = 0; i < options.size(); i++) {
            sb.append(System.lineSeparator())
              .append("   ").append((char) ('A' + i)).append(") ").append(options.get(i));
        }
        return sb.toString();
    }

    @Override
    public String getAnswerHint() {
        return "A-" + (char) ('A' + options.size() - 1);
    }

    @Override
    public boolean isValidAnswer(String answer) {
        return toIndex(answer) >= 0;
    }

    @Override
    public boolean isCorrect(String answer) {
        return toIndex(answer) == correctIndex;
    }

    @Override
    public String getCorrectAnswerText() {
        return (char) ('A' + correctIndex) + ") " + options.get(correctIndex);
    }
}
