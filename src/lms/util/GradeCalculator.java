/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.util;

/** Converts a percentage into a letter grade. */
public final class GradeCalculator {
    private GradeCalculator() {
        // utility class: no objects
    }

    public static String letter(double percent) {
        if (percent >= 90) {
            return "A+";
        }
        if (percent >= 80) {
            return "A";
        }
        if (percent >= 70) {
            return "B";
        }
        if (percent >= 60) {
            return "C";
        }
        if (percent >= 50) {
            return "D";
        }
        return "F";
    }
}
