/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** The three kinds of user account. The prefix is used when generating IDs. */
public enum Role {
    STUDENT("S"), INSTRUCTOR("I"), ADMIN("A");

    private final String idPrefix;

    Role(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public String getIdPrefix() {
        return idPrefix;
    }
}
