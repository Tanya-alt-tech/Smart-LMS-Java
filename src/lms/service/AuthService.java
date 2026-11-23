/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.util.regex.Pattern;

import lms.exception.AuthenticationException;
import lms.exception.DuplicateEntryException;
import lms.exception.NotFoundException;
import lms.exception.ValidationException;
import lms.model.Admin;
import lms.model.Instructor;
import lms.model.Role;
import lms.model.Student;
import lms.model.User;

/** Registration, login and account management. */
public class AuthService {
    public static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final DataStore store;

    public AuthService(DataStore store) {
        this.store = store;
    }

    public Student registerStudent(String name, String email, String password, String interest)
            throws ValidationException, DuplicateEntryException {
        String cleanEmail = validate(name, email, password);
        requireText(interest, "Interest area");
        Student student = new Student(store.nextUserId(Role.STUDENT), name.trim(), cleanEmail,
                password, interest.trim());
        store.addUser(student);
        return student;
    }

    public Instructor registerInstructor(String name, String email, String password, String specialization)
            throws ValidationException, DuplicateEntryException {
        String cleanEmail = validate(name, email, password);
        requireText(specialization, "Specialization");
        Instructor instructor = new Instructor(store.nextUserId(Role.INSTRUCTOR), name.trim(), cleanEmail,
                password, specialization.trim());
        store.addUser(instructor);
        return instructor;
    }

    public Admin registerAdmin(String name, String email, String password)
            throws ValidationException, DuplicateEntryException {
        String cleanEmail = validate(name, email, password);
        Admin admin = new Admin(store.nextUserId(Role.ADMIN), name.trim(), cleanEmail, password);
        store.addUser(admin);
        return admin;
    }

    /** The same message is used for a wrong email and a wrong password, so neither can be guessed. */
    public User login(String email, String password) throws AuthenticationException {
        User user = store.findUserByEmail(email);
        if (user == null || password == null || !user.checkPassword(password)) {
            throw new AuthenticationException("Invalid email or password.");
        }
        if (!user.isActive()) {
            throw new AuthenticationException("This account has been deactivated. Contact the administrator.");
        }
        return user;
    }

    public void changePassword(User user, String oldPassword, String newPassword)
            throws AuthenticationException, ValidationException {
        if (oldPassword == null || !user.checkPassword(oldPassword)) {
            throw new AuthenticationException("Current password is incorrect.");
        }
        validatePassword(newPassword);
        if (newPassword.equals(oldPassword)) {
            throw new ValidationException("The new password must be different from the current one.");
        }
        user.changePassword(newPassword);
    }

    public User setActive(User actingAdmin, String userId, boolean active)
            throws NotFoundException, ValidationException {
        User target = store.findUserById(userId);
        if (target == null) {
            throw new NotFoundException("No user with ID " + userId + ".");
        }
        if (target.equals(actingAdmin)) {
            throw new ValidationException("You cannot change the status of your own account.");
        }
        target.setActive(active);
        return target;
    }

    // ---------- validation helpers ----------

    private String validate(String name, String email, String password)
            throws ValidationException, DuplicateEntryException {
        requireText(name, "Name");
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        validatePassword(password);
        String cleanEmail = email.trim().toLowerCase();
        if (store.findUserByEmail(cleanEmail) != null) {
            throw new DuplicateEntryException("An account with this email already exists.");
        }
        return cleanEmail;
    }

    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private void requireText(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " cannot be empty.");
        }
    }
}
