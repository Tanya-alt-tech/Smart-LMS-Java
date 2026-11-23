/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.exception;

/**
 * Base class of every checked exception raised by the LMS.
 * The user interface catches this single type and shows the message,
 * so no business-rule failure can crash the program.
 */
public class LmsException extends Exception {
    private static final long serialVersionUID = 1L;

    public LmsException(String message) {
        super(message);
    }
}
