/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** A statement the student marks as true or false (T / F / TRUE / FALSE). */
public class TrueFalseQuestion extends Question {
    private static final long serialVersionUID = 1L;

    private final boolean correctAnswer;

    public TrueFalseQuestion(String text, int marks, boolean correctAnswer) {
        super(text, marks);
        this.correctAnswer = correctAnswer;
    }

    /** @return TRUE, FALSE, or null when the text is neither */
    private Boolean parse(String answer) {
        if (answer == null) {
            return null;
        }
        String a = answer.trim().toUpperCase();
        if (a.equals("T") || a.equals("TRUE")) {
            return Boolean.TRUE;
        }
        if (a.equals("F") || a.equals("FALSE")) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public String getPrompt() {
        return getText() + " (True / False)";
    }

    @Override
    public String getAnswerHint() {
        return "T/F";
    }

    @Override
    public boolean isValidAnswer(String answer) {
        return parse(answer) != null;
    }

    @Override
    public boolean isCorrect(String answer) {
        Boolean parsed = parse(answer);
        return parsed != null && parsed.booleanValue() == correctAnswer;
    }

    @Override
    public String getCorrectAnswerText() {
        return correctAnswer ? "True" : "False";
    }
}
