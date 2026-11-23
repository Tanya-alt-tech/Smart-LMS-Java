/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.exception;

/** Thrown for a specific kind of LMS failure; see {@link LmsException}. */
public class DuplicateEntryException extends LmsException {
    private static final long serialVersionUID = 1L;

    public DuplicateEntryException(String message) {
        super(message);
    }
}
