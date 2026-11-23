/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

/** Anything whose completion can be measured as a percentage. */
public interface Trackable {
    /** @return progress from 0.0 to 100.0 */
    double getProgressPercent();

    boolean isCompleted();
}
