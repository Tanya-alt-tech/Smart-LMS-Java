/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.util.ArrayList;
import java.util.List;

import lms.exception.NotFoundException;
import lms.exception.ValidationException;
import lms.model.Course;
import lms.model.Instructor;
import lms.model.Lesson;
import lms.model.Level;
import lms.model.Quiz;

/** Creating courses and adding lessons and quizzes to them. */
public class CourseService {
    private final DataStore store;

    public CourseService(DataStore store) {
        this.store = store;
    }

    public Course createCourse(Instructor instructor, String title, String category, Level level,
                               String description) throws ValidationException {
        requireText(title, "Course title");
        requireText(category, "Category");
        requireText(description, "Description");
        if (level == null) {
            throw new ValidationException("Level is required.");
        }
        Course course = new Course(store.nextCourseId(), title.trim(), category.trim(), level,
                description.trim(), instructor);
        store.addCourse(course);
        return course;
    }

    public Lesson addLesson(Instructor instructor, Course course, String title, String content,
                            int durationMinutes) throws ValidationException {
        requireOwner(instructor, course);
        requireText(title, "Lesson title");
        requireText(content, "Lesson content");
        if (durationMinutes < 1 || durationMinutes > 600) {
            throw new ValidationException("Duration must be between 1 and 600 minutes.");
        }
        Lesson lesson = new Lesson(title.trim(), content.trim(), durationMinutes);
        course.addLesson(lesson);
        return lesson;
    }

    public void addQuiz(Instructor instructor, Course course, Quiz quiz) throws ValidationException {
        requireOwner(instructor, course);
        requireText(quiz.getTitle(), "Quiz title");
        if (quiz.getQuestions().isEmpty()) {
            throw new ValidationException("A quiz needs at least one question.");
        }
        if (quiz.getPassPercent() < 1 || quiz.getPassPercent() > 100) {
            throw new ValidationException("Pass mark must be between 1 and 100 percent.");
        }
        course.addQuiz(quiz);
    }

    public Course getCourse(String id) throws NotFoundException {
        Course course = store.findCourseById(id);
        if (course == null) {
            throw new NotFoundException("No course with ID " + id + ".");
        }
        return course;
    }

    public List<Course> listAll() {
        return store.getCourses();
    }

    public List<Course> coursesBy(Instructor instructor) {
        List<Course> result = new ArrayList<Course>();
        for (Course course : store.getCourses()) {
            if (course.getInstructor().equals(instructor)) {
                result.add(course);
            }
        }
        return result;
    }

    /** Case-insensitive search in title, category, level and description. */
    public List<Course> search(String keyword) {
        List<Course> result = new ArrayList<Course>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }
        String k = keyword.trim().toLowerCase();
        for (Course course : store.getCourses()) {
            if (course.getTitle().toLowerCase().contains(k)
                    || course.getCategory().toLowerCase().contains(k)
                    || course.getLevel().name().toLowerCase().contains(k)
                    || course.getDescription().toLowerCase().contains(k)) {
                result.add(course);
            }
        }
        return result;
    }

    private void requireOwner(Instructor instructor, Course course) throws ValidationException {
        if (!course.getInstructor().equals(instructor)) {
            throw new ValidationException("Only the instructor who owns a course can change it.");
        }
    }

    private void requireText(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " cannot be empty.");
        }
    }
}
