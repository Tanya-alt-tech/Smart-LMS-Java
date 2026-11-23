/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;

import lms.util.PasswordUtil;

/**
 * Abstract parent of Student, Instructor and Admin.
 * Fields are private (encapsulation); the password is never stored,
 * only a salted SHA-256 hash of it.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String email;
    private String name;
    private String salt;
    private String passwordHash;
    private boolean active = true;

    protected User(String id, String name, String email, String rawPassword) {
        this.id = id;
        this.name = name;
        this.email = email;
        changePassword(rawPassword);
    }

    /** Each subclass states its own role (polymorphism). */
    public abstract Role getRole();

    /** One-line description; each subclass adds its own details. */
    public abstract String describe();

    public boolean checkPassword(String rawPassword) {
        return PasswordUtil.hash(rawPassword, salt).equals(passwordHash);
    }

    public final void changePassword(String rawPassword) {
        this.salt = PasswordUtil.newSalt();
        this.passwordHash = PasswordUtil.hash(rawPassword, salt);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof User && ((User) other).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%-5s %-22s %-28s %-10s %s",
                id, name, email, getRole(), active ? "Active" : "Inactive");
    }
}
