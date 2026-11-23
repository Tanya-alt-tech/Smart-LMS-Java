/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** Difficulty level of a course, in increasing order. */
public enum Level {
    BEGINNER, INTERMEDIATE, ADVANCED;

    /** The level a learner should move to after finishing this one. */
    public Level next() {
        return this == ADVANCED ? ADVANCED : values()[ordinal() + 1];
    }
}
