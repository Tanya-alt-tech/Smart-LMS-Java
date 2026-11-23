/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A set of questions with a pass mark. Grades a list of answers automatically. */
public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final int passPercent;
    private final List<Question> questions = new ArrayList<Question>();

    public Quiz(String title, int passPercent) {
        this.title = title;
        this.passPercent = passPercent;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public String getTitle() {
        return title;
    }

    public int getPassPercent() {
        return passPercent;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public int getTotalMarks() {
        int total = 0;
        for (Question q : questions) {
            total += q.getMarks();
        }
        return total;
    }

    /**
     * Grades the answers, one per question and in the same order.
     * Each question checks its own answer, whatever its type.
     */
    public QuizResult grade(List<String> answers) {
        if (answers == null || answers.size() != questions.size()) {
            throw new IllegalArgumentException("Exactly one answer is required for each question.");
        }
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.isCorrect(answers.get(i))) {
                score += q.getMarks();
            }
        }
        return new QuizResult(score, getTotalMarks(), LocalDate.now());
    }
}
