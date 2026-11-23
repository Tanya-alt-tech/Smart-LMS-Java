/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.model;

import java.io.Serializable;

/** One unit of study material inside a course. */
public class Lesson implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private final String content;
    private final int durationMinutes;

    public Lesson(String title, String content, int durationMinutes) {
        this.title = title;
        this.content = content;
        this.durationMinutes = durationMinutes;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
