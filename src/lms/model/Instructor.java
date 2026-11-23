/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** A teacher who creates courses, lessons and quizzes. */
public class Instructor extends User {
    private static final long serialVersionUID = 1L;

    private final String specialization;

    public Instructor(String id, String name, String email, String rawPassword, String specialization) {
        super(id, name, email, rawPassword);
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }

    @Override
    public Role getRole() {
        return Role.INSTRUCTOR;
    }

    @Override
    public String describe() {
        return "Instructor " + getName() + " (specialization: " + specialization + ")";
    }
}
