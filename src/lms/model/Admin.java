/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** A system administrator who manages user accounts and views reports. */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String name, String email, String rawPassword) {
        super(id, name, email, rawPassword);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String describe() {
        return "Administrator " + getName();
    }
}
