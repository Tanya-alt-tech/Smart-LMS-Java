/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;
import java.time.LocalDate;

/** The outcome of one quiz attempt. Immutable. */
public class QuizResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int score;
    private final int totalMarks;
    private final LocalDate takenOn;

    public QuizResult(int score, int totalMarks, LocalDate takenOn) {
        this.score = score;
        this.totalMarks = totalMarks;
        this.takenOn = takenOn;
    }

    public int getScore() {
        return score;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public LocalDate getTakenOn() {
        return takenOn;
    }

    public double getPercent() {
        return totalMarks == 0 ? 0.0 : score * 100.0 / totalMarks;
    }
}
