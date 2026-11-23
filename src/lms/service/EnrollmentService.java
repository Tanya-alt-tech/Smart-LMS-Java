/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lms.exception.DuplicateEntryException;
import lms.exception.NotFoundException;
import lms.exception.ValidationException;
import lms.model.Course;
import lms.model.Enrollment;
import lms.model.Quiz;
import lms.model.QuizResult;
import lms.model.Student;
import lms.util.GradeCalculator;

/** Enrolling students and recording their lesson and quiz activity. */
public class EnrollmentService {
    private final DataStore store;

    public EnrollmentService(DataStore store) {
        this.store = store;
    }

    public Enrollment enroll(Student student, Course course) throws DuplicateEntryException {
        if (find(student, course) != null) {
            throw new DuplicateEntryException("You are already enrolled in " + course.getTitle() + ".");
        }
        Enrollment enrollment = new Enrollment(student, course, LocalDate.now());
        store.addEnrollment(enrollment);
        return enrollment;
    }

    /** @return the enrollment, or null if the student is not in the course */
    public Enrollment find(Student student, Course course) {
        for (Enrollment e : store.getEnrollments()) {
            if (e.getStudent().equals(student) && e.getCourse().equals(course)) {
                return e;
            }
        }
        return null;
    }

    public Enrollment get(Student student, Course course) throws NotFoundException {
        Enrollment enrollment = find(student, course);
        if (enrollment == null) {
            throw new NotFoundException("You are not enrolled in " + course.getTitle() + ".");
        }
        return enrollment;
    }

    public List<Enrollment> enrollmentsOf(Student student) {
        List<Enrollment> result = new ArrayList<Enrollment>();
        for (Enrollment e : store.getEnrollments()) {
            if (e.getStudent().equals(student)) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Enrollment> enrollmentsIn(Course course) {
        List<Enrollment> result = new ArrayList<Enrollment>();
        for (Enrollment e : store.getEnrollments()) {
            if (e.getCourse().equals(course)) {
                result.add(e);
            }
        }
        return result;
    }

    /** @return true if newly completed, false if it was already completed */
    public boolean completeLesson(Enrollment enrollment, int lessonIndex) throws ValidationException {
        if (lessonIndex < 0 || lessonIndex >= enrollment.getCourse().getLessons().size()) {
            throw new ValidationException("That lesson does not exist.");
        }
        return enrollment.markLessonComplete(lessonIndex);
    }

    /** Grades the answers, stores the attempt and returns the result. */
    public QuizResult submitQuiz(Enrollment enrollment, int quizIndex, List<String> answers)
            throws ValidationException {
        List<Quiz> quizzes = enrollment.getCourse().getQuizzes();
        if (quizIndex < 0 || quizIndex >= quizzes.size()) {
            throw new ValidationException("That quiz does not exist.");
        }
        Quiz quiz = quizzes.get(quizIndex);
        if (answers == null || answers.size() != quiz.getQuestions().size()) {
            throw new ValidationException("Every question must be answered.");
        }
        QuizResult result = quiz.grade(answers);
        enrollment.recordAttempt(quizIndex, result);
        return result;
    }

    /** Students of a course ranked by average quiz score, then by progress, then by name. */
    public List<Enrollment> leaderboard(Course course) {
        List<Enrollment> list = enrollmentsIn(course);
        Collections.sort(list, new Comparator<Enrollment>() {
            @Override
            public int compare(Enrollment a, Enrollment b) {
                int byScore = Double.compare(b.getAverageScore(), a.getAverageScore());
                if (byScore != 0) {
                    return byScore;
                }
                int byProgress = Double.compare(b.getProgressPercent(), a.getProgressPercent());
                if (byProgress != 0) {
                    return byProgress;
                }
                return a.getStudent().getName().compareToIgnoreCase(b.getStudent().getName());
            }
        });
        return list;
    }

    /** Builds the certificate text. Only allowed once the course is fully completed. */
    public String buildCertificate(Enrollment enrollment) throws ValidationException {
        if (!enrollment.isCompleted()) {
            throw new ValidationException(String.format(
                    "Certificate not available yet: the course is %.0f%% complete. "
                            + "Finish every lesson and pass every quiz first.",
                    enrollment.getProgressPercent()));
        }
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();
        boolean hasQuizzes = !course.getQuizzes().isEmpty();
        String nl = System.lineSeparator();
        String border = "************************************************************";
        StringBuilder sb = new StringBuilder();
        sb.append(border).append(nl);
        sb.append("               CERTIFICATE OF COMPLETION").append(nl);
        sb.append(border).append(nl).append(nl);
        sb.append("  This is to certify that").append(nl).append(nl);
        sb.append("      ").append(student.getName()).append("  (").append(student.getId()).append(")").append(nl).append(nl);
        sb.append("  has successfully completed the course").append(nl).append(nl);
        sb.append("      ").append(course.getTitle()).append("  [").append(course.getLevel()).append("]").append(nl).append(nl);
        if (hasQuizzes) {
            sb.append(String.format("  Final score : %.1f%%   Grade : %s",
                    enrollment.getAverageScore(), GradeCalculator.letter(enrollment.getAverageScore()))).append(nl);
        }
        sb.append("  Instructor  : ").append(course.getInstructor().getName()).append(nl);
        sb.append("  Issued on   : ").append(LocalDate.now()).append(nl);
        sb.append("  Certificate : CERT-").append(course.getId()).append("-").append(student.getId()).append(nl).append(nl);
        sb.append(border).append(nl);
        return sb.toString();
    }
}
