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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Links one student to one course and records everything the student
 * has done in it: lessons completed and every quiz attempt.
 */
public class Enrollment implements Trackable, Serializable {
    private static final long serialVersionUID = 1L;

    private final Student student;
    private final Course course;
    private final LocalDate enrolledOn;
    private final Set<Integer> completedLessons = new TreeSet<Integer>();
    private final Map<Integer, List<QuizResult>> attempts = new TreeMap<Integer, List<QuizResult>>();

    public Enrollment(Student student, Course course, LocalDate enrolledOn) {
        this.student = student;
        this.course = course;
        this.enrolledOn = enrolledOn;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }

    // ---------- lessons ----------

    /** @return false if the lesson was already completed */
    public boolean markLessonComplete(int lessonIndex) {
        if (lessonIndex < 0 || lessonIndex >= course.getLessons().size()) {
            throw new IndexOutOfBoundsException("No such lesson: " + (lessonIndex + 1));
        }
        return completedLessons.add(lessonIndex);
    }

    public boolean isLessonComplete(int lessonIndex) {
        return completedLessons.contains(lessonIndex);
    }

    public int getCompletedLessonCount() {
        return completedLessons.size();
    }

    // ---------- quizzes ----------

    public void recordAttempt(int quizIndex, QuizResult result) {
        if (quizIndex < 0 || quizIndex >= course.getQuizzes().size()) {
            throw new IndexOutOfBoundsException("No such quiz: " + (quizIndex + 1));
        }
        List<QuizResult> list = attempts.get(quizIndex);
        if (list == null) {
            list = new ArrayList<QuizResult>();
            attempts.put(quizIndex, list);
        }
        list.add(result);
    }

    public List<QuizResult> getAttempts(int quizIndex) {
        List<QuizResult> list = attempts.get(quizIndex);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    /** @return the attempt with the highest percentage, or null if the quiz was never taken */
    public QuizResult getBestResult(int quizIndex) {
        QuizResult best = null;
        for (QuizResult r : getAttempts(quizIndex)) {
            if (best == null || r.getPercent() > best.getPercent()) {
                best = r;
            }
        }
        return best;
    }

    public boolean isQuizPassed(int quizIndex) {
        QuizResult best = getBestResult(quizIndex);
        return best != null && best.getPercent() >= course.getQuizzes().get(quizIndex).getPassPercent();
    }

    public int getPassedQuizCount() {
        int passed = 0;
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            if (isQuizPassed(i)) {
                passed++;
            }
        }
        return passed;
    }

    public boolean hasQuizAttempts() {
        return !attempts.isEmpty();
    }

    /** Mean of the best percentage in every quiz attempted so far (0 if none). */
    public double getAverageScore() {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            QuizResult best = getBestResult(i);
            if (best != null) {
                sum += best.getPercent();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    // ---------- Trackable ----------

    /** Share of the course finished: completed lessons plus passed quizzes, over all items. */
    @Override
    public double getProgressPercent() {
        int total = course.getLessons().size() + course.getQuizzes().size();
        if (total == 0) {
            return 0.0;
        }
        return (completedLessons.size() + getPassedQuizCount()) * 100.0 / total;
    }

    /** True when every lesson is completed and every quiz is passed. */
    @Override
    public boolean isCompleted() {
        int total = course.getLessons().size() + course.getQuizzes().size();
        return total > 0
                && completedLessons.size() == course.getLessons().size()
                && getPassedQuizCount() == course.getQuizzes().size();
    }
}
