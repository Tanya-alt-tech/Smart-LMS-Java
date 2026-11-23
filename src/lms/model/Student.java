/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** A learner. The interest area feeds the recommendation engine. */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String interest;

    public Student(String id, String name, String email, String rawPassword, String interest) {
        super(id, name, email, rawPassword);
        this.interest = interest;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    @Override
    public String describe() {
        return "Student " + getName() + " (interested in " + interest + ")";
    }
}
