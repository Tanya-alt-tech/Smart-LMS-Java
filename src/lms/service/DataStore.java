/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lms.exception.LmsException;
import lms.model.Course;
import lms.model.Enrollment;
import lms.model.Role;
import lms.model.User;

/**
 * Holds all data of the system and saves it to / loads it from a file
 * using Java object serialization, so nothing is lost between runs.
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, User> users = new LinkedHashMap<String, User>();
    private final Map<String, Course> courses = new LinkedHashMap<String, Course>();
    private final List<Enrollment> enrollments = new ArrayList<Enrollment>();
    private int userCounter;
    private int courseCounter;

    /** Where to save. Not serialized; null means "keep in memory only". */
    private transient File file;
    private transient String loadNote;

    private DataStore() {
    }

    /** An empty store that is never written to disk (used by the self-test). */
    public static DataStore inMemory() {
        return new DataStore();
    }

    /**
     * Loads the store from the file. If the file does not exist a new store
     * with the default accounts and courses is created; if it cannot be read it is kept aside as
     * a ".corrupt" file and a fresh store is created instead.
     */
    public static DataStore load(File file) {
        DataStore store = null;
        String note;
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                store = (DataStore) in.readObject();
                note = "Saved data loaded from " + file.getName() + ".";
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                File backup = new File(file.getPath() + ".corrupt");
                backup.delete();
                boolean moved = file.renameTo(backup);
                note = "Warning: " + file.getName() + " could not be read"
                        + (moved ? " (kept as " + backup.getName() + ")" : "")
                        + ". Starting again with the default accounts and courses.";
            }
        } else {
            note = "First run: default accounts and courses created.";
        }
        if (store == null) {
            store = new DataStore();
            try {
                SeedData.populate(store);
            } catch (LmsException e) {
                throw new IllegalStateException("Default data is invalid: " + e.getMessage(), e);
            }
            store.file = file;
            store.save();
        }
        store.file = file;
        store.loadNote = note;
        return store;
    }

    /** Writes everything to the data file. A failure is reported but never crashes the program. */
    public void save() {
        if (file == null) {
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        } catch (IOException e) {
            System.out.println("Warning: could not save data (" + e.getMessage() + ").");
        }
    }

    public String getLoadNote() {
        return loadNote;
    }

    // ---------- users ----------

    String nextUserId(Role role) {
        userCounter++;
        return role.getIdPrefix() + String.format("%03d", userCounter);
    }

    void addUser(User user) {
        users.put(user.getId(), user);
    }

    public User findUserById(String id) {
        return id == null ? null : users.get(id.trim().toUpperCase());
    }

    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        String wanted = email.trim().toLowerCase();
        for (User user : users.values()) {
            if (user.getEmail().equals(wanted)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

    // ---------- courses ----------

    String nextCourseId() {
        courseCounter++;
        return "C" + String.format("%03d", courseCounter);
    }

    void addCourse(Course course) {
        courses.put(course.getId(), course);
    }

    public Course findCourseById(String id) {
        return id == null ? null : courses.get(id.trim().toUpperCase());
    }

    public List<Course> getCourses() {
        return new ArrayList<Course>(courses.values());
    }

    // ---------- enrollments ----------

    void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    public List<Enrollment> getEnrollments() {
        return new ArrayList<Enrollment>(enrollments);
    }
}
