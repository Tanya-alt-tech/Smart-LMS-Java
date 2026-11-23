/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A course owned by one instructor and made up of lessons and quizzes (composition). */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String title;
    private final String category;
    private final Level level;
    private final String description;
    private final Instructor instructor;
    private final List<Lesson> lessons = new ArrayList<Lesson>();
    private final List<Quiz> quizzes = new ArrayList<Quiz>();

    public Course(String id, String title, String category, Level level,
                  String description, Instructor instructor) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.level = level;
        this.description = description;
        this.instructor = instructor;
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public Level getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public List<Quiz> getQuizzes() {
        return Collections.unmodifiableList(quizzes);
    }

    public int getTotalDurationMinutes() {
        int total = 0;
        for (Lesson lesson : lessons) {
            total += lesson.getDurationMinutes();
        }
        return total;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Course && ((Course) other).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%-5s %-38s %-16s %-12s %d lessons, %d quizzes",
                id, title, category, level, lessons.size(), quizzes.size());
    }
}
