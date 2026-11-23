/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lms.model.Course;
import lms.model.Enrollment;
import lms.model.Level;
import lms.model.Quiz;
import lms.model.QuizResult;
import lms.model.Student;

/**
 * The "smart" part of the LMS. It uses rules over the student's interest,
 * history and scores to:
 *   1. recommend the most suitable courses, with reasons;
 *   2. tell the student the next step in a course;
 *   3. point out weak areas that need revision;
 *   4. flag at-risk students for the instructor.
 */
public class RecommendationService {
    /** A best quiz score below this is treated as a weak area. */
    public static final double WEAK_SCORE = 60.0;
    /** A completed course counts toward "levelling up" only with at least this average. */
    public static final double LEVEL_UP_SCORE = 60.0;
    /** Progress below this, or an average below AT_RISK_SCORE, marks a student as at risk. */
    public static final double AT_RISK_PROGRESS = 25.0;
    public static final double AT_RISK_SCORE = 50.0;

    /** A recommended course together with its score and the reasons for it. */
    public static class Recommendation {
        private final Course course;
        private final int score;
        private final String reason;

        Recommendation(Course course, int score, String reason) {
            this.course = course;
            this.score = score;
            this.reason = reason;
        }

        public Course getCourse() {
            return course;
        }

        public int getScore() {
            return score;
        }

        public String getReason() {
            return reason;
        }
    }

    private final DataStore store;
    private final EnrollmentService enrollmentService;

    public RecommendationService(DataStore store, EnrollmentService enrollmentService) {
        this.store = store;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Scores every course the student has not joined:
     *   +3  category matches the student's stated interest
     *   +2  student already studies that category
     *   +2  level is exactly right (Beginner first, then one level above
     *       the highest level completed in that category with a good score)
     *   -2  level is too advanced for now
     * Only courses with a positive score are returned, best first.
     * Ties are broken by popularity (number of enrolled students).
     */
    public List<Recommendation> recommendCourses(Student student, int limit) {
        List<Enrollment> mine = enrollmentService.enrollmentsOf(student);
        List<Recommendation> result = new ArrayList<Recommendation>();

        for (Course course : store.getCourses()) {
            if (enrollmentService.find(student, course) != null) {
                continue;
            }
            int score = 0;
            List<String> reasons = new ArrayList<String>();

            if (course.getCategory().equalsIgnoreCase(student.getInterest())) {
                score += 3;
                reasons.add("matches your interest in " + course.getCategory());
            }
            if (studiesCategory(mine, course.getCategory())) {
                score += 2;
                reasons.add("builds on what you already study");
            }
            Level target = targetLevel(mine, course.getCategory());
            if (course.getLevel() == target) {
                score += 2;
                reasons.add("right level for you (" + target + ")");
            } else if (course.getLevel().ordinal() > target.ordinal()) {
                score -= 2;
                reasons.add("advanced - finish a " + target + " course first");
            }

            if (score > 0) {
                result.add(new Recommendation(course, score, join(reasons)));
            }
        }

        Collections.sort(result, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation a, Recommendation b) {
                if (a.score != b.score) {
                    return b.score - a.score;
                }
                int popA = enrollmentService.enrollmentsIn(a.course).size();
                int popB = enrollmentService.enrollmentsIn(b.course).size();
                if (popA != popB) {
                    return popB - popA;
                }
                return a.course.getId().compareTo(b.course.getId());
            }
        });
        return result.size() > limit ? new ArrayList<Recommendation>(result.subList(0, limit)) : result;
    }

    private boolean studiesCategory(List<Enrollment> mine, String category) {
        for (Enrollment e : mine) {
            if (e.getCourse().getCategory().equalsIgnoreCase(category)) {
                return true;
            }
        }
        return false;
    }

    /** The level the student is ready for in a category. */
    private Level targetLevel(List<Enrollment> mine, String category) {
        Level target = Level.BEGINNER;
        for (Enrollment e : mine) {
            Course c = e.getCourse();
            boolean goodScore = c.getQuizzes().isEmpty() || e.getAverageScore() >= LEVEL_UP_SCORE;
            if (c.getCategory().equalsIgnoreCase(category) && e.isCompleted() && goodScore) {
                Level next = c.getLevel().next();
                if (next.ordinal() > target.ordinal()) {
                    target = next;
                }
            }
        }
        return target;
    }

    /** One sentence telling the student what to do next in a course. */
    public String nextStep(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        if (course.getLessons().isEmpty() && course.getQuizzes().isEmpty()) {
            return "No content yet - check back when the instructor adds lessons.";
        }
        for (int i = 0; i < course.getLessons().size(); i++) {
            if (!enrollment.isLessonComplete(i)) {
                return "Study Lesson " + (i + 1) + ": " + course.getLessons().get(i).getTitle();
            }
        }
        for (int i = 0; i < course.getQuizzes().size(); i++) {
            Quiz quiz = course.getQuizzes().get(i);
            QuizResult best = enrollment.getBestResult(i);
            if (best == null) {
                return "Attempt the quiz: " + quiz.getTitle();
            }
            if (!enrollment.isQuizPassed(i)) {
                return String.format("Revise and retake the quiz: %s (best %.0f%%, need %d%%)",
                        quiz.getTitle(), best.getPercent(), quiz.getPassPercent());
            }
        }
        return "Course completed - collect your certificate!";
    }

    /** Quizzes, across all the student's courses, whose best score is below WEAK_SCORE. */
    public List<String> weakAreas(Student student) {
        List<String> result = new ArrayList<String>();
        for (Enrollment e : enrollmentService.enrollmentsOf(student)) {
            Course course = e.getCourse();
            for (int i = 0; i < course.getQuizzes().size(); i++) {
                QuizResult best = e.getBestResult(i);
                if (best != null && best.getPercent() < WEAK_SCORE) {
                    result.add(String.format("%s > %s: best score %.0f%% - revise the lessons and retake it",
                            course.getTitle(), course.getQuizzes().get(i).getTitle(), best.getPercent()));
                }
            }
        }
        return result;
    }

    /** Students of a course who have not finished and are falling behind, with the reason. */
    public List<String> atRiskStudents(Course course) {
        List<String> result = new ArrayList<String>();
        for (Enrollment e : enrollmentService.enrollmentsIn(course)) {
            if (e.isCompleted()) {
                continue;
            }
            List<String> reasons = new ArrayList<String>();
            if (e.getProgressPercent() < AT_RISK_PROGRESS) {
                reasons.add(String.format("low progress (%.0f%%)", e.getProgressPercent()));
            }
            if (e.hasQuizAttempts() && e.getAverageScore() < AT_RISK_SCORE) {
                reasons.add(String.format("low quiz average (%.0f%%)", e.getAverageScore()));
            }
            if (!reasons.isEmpty()) {
                result.add(e.getStudent().getName() + " (" + e.getStudent().getId() + "): " + join(reasons));
            }
        }
        return result;
    }

    private static String join(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
