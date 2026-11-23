/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import lms.exception.LmsException;
import lms.model.Admin;
import lms.model.Course;
import lms.model.Enrollment;
import lms.model.Instructor;
import lms.model.Lesson;
import lms.model.Level;
import lms.model.MultipleChoiceQuestion;
import lms.model.Question;
import lms.model.Quiz;
import lms.model.QuizResult;
import lms.model.Role;
import lms.model.Student;
import lms.model.TrueFalseQuestion;
import lms.model.User;
import lms.service.AuthService;
import lms.service.CourseService;
import lms.service.DataStore;
import lms.service.EnrollmentService;
import lms.service.RecommendationService;
import lms.service.RecommendationService.Recommendation;
import lms.util.GradeCalculator;
import lms.util.InputHelper;
import lms.util.InputHelper.InputClosedException;

/**
 * Menu-driven console interface. It only reads input and prints output;
 * every rule lives in the service classes.
 */
public class ConsoleUI {
    private static final String LINE = "------------------------------------------------------------------------";
    private static final String DOUBLE_LINE = "========================================================================";

    private final DataStore store;
    private final InputHelper in;
    private final AuthService auth;
    private final CourseService courses;
    private final EnrollmentService enrollments;
    private final RecommendationService smart;

    public ConsoleUI(DataStore store, InputStream input) {
        this.store = store;
        this.in = new InputHelper(input);
        this.auth = new AuthService(store);
        this.courses = new CourseService(store);
        this.enrollments = new EnrollmentService(store);
        this.smart = new RecommendationService(store, enrollments);
    }

    // =====================================================================
    //  Main menu
    // =====================================================================

    public void run() {
        System.out.println(DOUBLE_LINE);
        System.out.println("              SMART LEARNING MANAGEMENT SYSTEM (LMS)");
        System.out.println(DOUBLE_LINE);
        if (store.getLoadNote() != null) {
            System.out.println(store.getLoadNote());
        }
        try {
            boolean running = true;
            while (running) {
                heading("MAIN MENU");
                System.out.println(" 1. Login");
                System.out.println(" 2. Register as a new student");
                System.out.println(" 3. Show default accounts");
                System.out.println(" 0. Exit");
                int choice = in.readInt("Choose: ", 0, 3);
                try {
                    switch (choice) {
                        case 1: login(); break;
                        case 2: registerStudent(); break;
                        case 3: showDefaultAccounts(); break;
                        default: running = false;
                    }
                } catch (LmsException e) {
                    error(e);
                }
            }
        } catch (InputClosedException e) {
            System.out.println();
            System.out.println("Input ended.");
        }
        store.save();
        System.out.println("Data saved. Goodbye!");
    }

    private void login() throws LmsException {
        heading("LOGIN");
        String email = in.readNonEmpty("Email   : ");
        String password = in.readPassword("Password: ");
        User user = auth.login(email, password);
        System.out.println("Welcome, " + user.describe() + ".");
        // The role decides which dashboard opens.
        if (user.getRole() == Role.STUDENT) {
            studentMenu((Student) user);
        } else if (user.getRole() == Role.INSTRUCTOR) {
            instructorMenu((Instructor) user);
        } else {
            adminMenu((Admin) user);
        }
    }

    private void registerStudent() throws LmsException {
        heading("STUDENT REGISTRATION");
        String name = in.readNonEmpty("Full name : ");
        String email = in.readNonEmpty("Email     : ");
        String password = in.readPassword("Password (min " + AuthService.MIN_PASSWORD_LENGTH + " characters): ");
        String interest = chooseCategory("Your main interest");
        Student student = auth.registerStudent(name, email, password, interest);
        store.save();
        System.out.println("Registered successfully. Your student ID is " + student.getId() + ". You can now log in.");
    }

    private void showDefaultAccounts() {
        heading("DEFAULT ACCOUNTS");
        System.out.println(" Admin      : admin@lms.com       / admin123");
        System.out.println(" Instructor : instructor@lms.com  / teach123");
        System.out.println(" Student    : student@lms.com     / learn123");
    }

    // =====================================================================
    //  Student
    // =====================================================================

    private void studentMenu(Student student) {
        while (true) {
            heading("STUDENT DASHBOARD - " + student.getName() + " (" + student.getId() + ")");
            System.out.println(" 1. Browse all courses");
            System.out.println(" 2. Search courses");
            System.out.println(" 3. Enroll in a course");
            System.out.println(" 4. My courses and progress");
            System.out.println(" 5. Study a lesson");
            System.out.println(" 6. Take a quiz");
            System.out.println(" 7. Smart recommendations");
            System.out.println(" 8. Report card");
            System.out.println(" 9. Get certificate");
            System.out.println("10. Change password");
            System.out.println(" 0. Logout");
            int choice = in.readInt("Choose: ", 0, 10);
            try {
                switch (choice) {
                    case 1: printCourses(courses.listAll()); break;
                    case 2: searchCourses(); break;
                    case 3: enroll(student); break;
                    case 4: showMyCourses(student); break;
                    case 5: studyLesson(student); break;
                    case 6: takeQuiz(student); break;
                    case 7: showRecommendations(student); break;
                    case 8: showReportCard(student); break;
                    case 9: getCertificate(student); break;
                    case 10: changePassword(student); break;
                    default: return;
                }
            } catch (LmsException e) {
                error(e);
            }
        }
    }

    private void searchCourses() {
        String keyword = in.readNonEmpty("Keyword (title, category or level): ");
        List<Course> found = courses.search(keyword);
        if (found.isEmpty()) {
            System.out.println("No courses match \"" + keyword + "\".");
        } else {
            printCourses(found);
        }
    }

    private void enroll(Student student) throws LmsException {
        List<Course> available = new ArrayList<Course>();
        for (Course c : courses.listAll()) {
            if (enrollments.find(student, c) == null) {
                available.add(c);
            }
        }
        if (available.isEmpty()) {
            System.out.println("You are already enrolled in every course.");
            return;
        }
        Course course = pickCourse(available, "Course to enroll in");
        if (course == null) {
            return;
        }
        enrollments.enroll(student, course);
        store.save();
        System.out.println("Enrolled in \"" + course.getTitle() + "\". Open 'Study a lesson' to begin.");
    }

    private void showMyCourses(Student student) {
        List<Enrollment> mine = enrollments.enrollmentsOf(student);
        if (mine.isEmpty()) {
            System.out.println("You are not enrolled in any course yet.");
            return;
        }
        heading("MY COURSES");
        for (Enrollment e : mine) {
            Course c = e.getCourse();
            System.out.println(c.getId() + "  " + c.getTitle() + "  [" + c.getLevel() + "]");
            System.out.printf("      Progress : %s %.0f%%%n", progressBar(e.getProgressPercent()), e.getProgressPercent());
            System.out.printf("      Lessons  : %d/%d completed    Quizzes: %d/%d passed%n",
                    e.getCompletedLessonCount(), c.getLessons().size(),
                    e.getPassedQuizCount(), c.getQuizzes().size());
            System.out.println("      Next step: " + smart.nextStep(e));
        }
    }

    private void studyLesson(Student student) throws LmsException {
        Enrollment e = pickEnrollment(student);
        if (e == null) {
            return;
        }
        List<Lesson> lessons = e.getCourse().getLessons();
        if (lessons.isEmpty()) {
            System.out.println("This course has no lessons yet.");
            return;
        }
        heading("LESSONS - " + e.getCourse().getTitle());
        for (int i = 0; i < lessons.size(); i++) {
            System.out.printf(" %d. [%s] %s (%d min)%n", i + 1,
                    e.isLessonComplete(i) ? "x" : " ", lessons.get(i).getTitle(),
                    lessons.get(i).getDurationMinutes());
        }
        int n = in.readInt("Lesson number (0 to cancel): ", 0, lessons.size());
        if (n == 0) {
            return;
        }
        Lesson lesson = lessons.get(n - 1);
        System.out.println(LINE);
        System.out.println("Lesson " + n + ": " + lesson.getTitle());
        System.out.println(LINE);
        printWrapped(lesson.getContent());
        System.out.println(LINE);
        if (e.isLessonComplete(n - 1)) {
            System.out.println("You have already completed this lesson.");
        } else if (in.readYesNo("Mark this lesson as completed?")) {
            enrollments.completeLesson(e, n - 1);
            store.save();
            System.out.printf("Lesson completed. Course progress: %.0f%%%n", e.getProgressPercent());
            System.out.println("Next step: " + smart.nextStep(e));
        }
    }

    private void takeQuiz(Student student) throws LmsException {
        Enrollment e = pickEnrollment(student);
        if (e == null) {
            return;
        }
        List<Quiz> quizzes = e.getCourse().getQuizzes();
        if (quizzes.isEmpty()) {
            System.out.println("This course has no quizzes yet.");
            return;
        }
        heading("QUIZZES - " + e.getCourse().getTitle());
        for (int i = 0; i < quizzes.size(); i++) {
            Quiz q = quizzes.get(i);
            QuizResult best = e.getBestResult(i);
            String status = best == null ? "not attempted"
                    : String.format("best %.0f%%, %d attempt(s), %s", best.getPercent(),
                            e.getAttempts(i).size(), e.isQuizPassed(i) ? "PASSED" : "not passed");
            System.out.printf(" %d. %s - %d questions, %d marks, pass at %d%% (%s)%n", i + 1, q.getTitle(),
                    q.getQuestions().size(), q.getTotalMarks(), q.getPassPercent(), status);
        }
        int n = in.readInt("Quiz number (0 to cancel): ", 0, quizzes.size());
        if (n == 0) {
            return;
        }
        Quiz quiz = quizzes.get(n - 1);
        heading("QUIZ: " + quiz.getTitle());
        List<Question> questions = quiz.getQuestions();
        List<String> answers = new ArrayList<String>();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println();
            System.out.println("Q" + (i + 1) + ". [" + q.getMarks() + " mark(s)] " + q.getPrompt());
            String answer;
            while (true) {
                answer = in.readLine("Your answer (" + q.getAnswerHint() + "): ");
                if (q.isValidAnswer(answer)) {
                    break;
                }
                System.out.println("  Invalid answer. Please enter " + q.getAnswerHint() + ".");
            }
            answers.add(answer);
        }
        QuizResult result = enrollments.submitQuiz(e, n - 1, answers);
        store.save();

        heading("RESULT");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            boolean ok = q.isCorrect(answers.get(i));
            System.out.println("Q" + (i + 1) + ": " + (ok ? "Correct" : "Wrong  ")
                    + (ok ? "" : "  -> correct answer: " + q.getCorrectAnswerText()));
        }
        System.out.println(LINE);
        boolean passed = result.getPercent() >= quiz.getPassPercent();
        System.out.printf("Score: %d / %d  (%.1f%%)   Grade: %s   %s%n", result.getScore(), result.getTotalMarks(),
                result.getPercent(), GradeCalculator.letter(result.getPercent()),
                passed ? "PASSED" : "NOT PASSED (need " + quiz.getPassPercent() + "%)");
        System.out.println("Next step: " + smart.nextStep(e));
    }

    private void showRecommendations(Student student) {
        heading("SMART RECOMMENDATIONS");
        System.out.println("Recommended courses for you:");
        List<Recommendation> recs = smart.recommendCourses(student, 3);
        if (recs.isEmpty()) {
            System.out.println("  No new courses to recommend right now.");
        }
        for (int i = 0; i < recs.size(); i++) {
            Recommendation r = recs.get(i);
            System.out.println("  " + (i + 1) + ". " + r.getCourse().getId() + " " + r.getCourse().getTitle()
                    + " [" + r.getCourse().getLevel() + "]");
            System.out.println("     Why: " + r.getReason());
        }
        System.out.println();
        System.out.println("What to do next in your courses:");
        List<Enrollment> mine = enrollments.enrollmentsOf(student);
        if (mine.isEmpty()) {
            System.out.println("  Enroll in a course to get a study plan.");
        }
        for (Enrollment e : mine) {
            System.out.println("  - " + e.getCourse().getTitle() + ": " + smart.nextStep(e));
        }
        System.out.println();
        System.out.println("Areas that need revision:");
        List<String> weak = smart.weakAreas(student);
        if (weak.isEmpty()) {
            System.out.println("  None - no quiz score is below "
                    + (int) RecommendationService.WEAK_SCORE + "%.");
        }
        for (String w : weak) {
            System.out.println("  - " + w);
        }
    }

    private void showReportCard(Student student) {
        List<Enrollment> mine = enrollments.enrollmentsOf(student);
        if (mine.isEmpty()) {
            System.out.println("You are not enrolled in any course yet.");
            return;
        }
        heading("REPORT CARD - " + student.getName() + " (" + student.getId() + ")");
        System.out.printf("%-38s %9s %9s %6s  %s%n", "Course", "Progress", "Average", "Grade", "Status");
        System.out.println(LINE);
        double sum = 0;
        int graded = 0;
        for (Enrollment e : mine) {
            String average = "-";
            String grade = "-";
            if (e.hasQuizAttempts()) {
                average = String.format("%.1f%%", e.getAverageScore());
                grade = GradeCalculator.letter(e.getAverageScore());
                sum += e.getAverageScore();
                graded++;
            }
            System.out.printf("%-38s %8.0f%% %9s %6s  %s%n", e.getCourse().getTitle(), e.getProgressPercent(),
                    average, grade, e.isCompleted() ? "Completed" : "In progress");
        }
        System.out.println(LINE);
        if (graded > 0) {
            System.out.printf("Overall average: %.1f%%  (Grade %s)%n", sum / graded,
                    GradeCalculator.letter(sum / graded));
        } else {
            System.out.println("Overall average: no quizzes attempted yet.");
        }
    }

    private void getCertificate(Student student) throws LmsException {
        Enrollment e = pickEnrollment(student);
        if (e == null) {
            return;
        }
        String certificate = enrollments.buildCertificate(e);
        System.out.println();
        System.out.print(certificate);
        File folder = new File("certificates");
        File file = new File(folder, "CERT-" + e.getCourse().getId() + "-" + student.getId() + ".txt");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            System.out.println("(The certificate could not be saved to a file.)");
            return;
        }
        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            out.print(certificate);
            System.out.println("Saved to " + file.getPath());
        } catch (IOException ex) {
            System.out.println("(The certificate could not be saved to a file: " + ex.getMessage() + ")");
        }
    }

    // =====================================================================
    //  Instructor
    // =====================================================================

    private void instructorMenu(Instructor instructor) {
        while (true) {
            heading("INSTRUCTOR DASHBOARD - " + instructor.getName() + " (" + instructor.getId() + ")");
            System.out.println(" 1. My courses");
            System.out.println(" 2. Create a course");
            System.out.println(" 3. Add a lesson");
            System.out.println(" 4. Add a quiz");
            System.out.println(" 5. Student progress");
            System.out.println(" 6. At-risk students (smart alert)");
            System.out.println(" 7. Leaderboard");
            System.out.println(" 8. Change password");
            System.out.println(" 0. Logout");
            int choice = in.readInt("Choose: ", 0, 8);
            try {
                switch (choice) {
                    case 1: showInstructorCourses(instructor); break;
                    case 2: createCourse(instructor); break;
                    case 3: addLesson(instructor); break;
                    case 4: addQuiz(instructor); break;
                    case 5: showStudentProgress(instructor); break;
                    case 6: showAtRisk(instructor); break;
                    case 7: showLeaderboard(instructor); break;
                    case 8: changePassword(instructor); break;
                    default: return;
                }
            } catch (LmsException e) {
                error(e);
            }
        }
    }

    private void showInstructorCourses(Instructor instructor) {
        List<Course> mine = courses.coursesBy(instructor);
        if (mine.isEmpty()) {
            System.out.println("You have not created any course yet.");
            return;
        }
        heading("MY COURSES");
        for (Course c : mine) {
            System.out.println(c);
            System.out.printf("      %d student(s) enrolled, %d minutes of lessons%n",
                    enrollments.enrollmentsIn(c).size(), c.getTotalDurationMinutes());
        }
    }

    private void createCourse(Instructor instructor) throws LmsException {
        heading("CREATE COURSE");
        String title = in.readNonEmpty("Title      : ");
        String category = chooseCategory("Category");
        System.out.println("Level: 1. BEGINNER  2. INTERMEDIATE  3. ADVANCED");
        Level level = Level.values()[in.readInt("Choose level: ", 1, 3) - 1];
        String description = in.readNonEmpty("Description: ");
        Course course = courses.createCourse(instructor, title, category, level, description);
        store.save();
        System.out.println("Course created with ID " + course.getId() + ". Now add lessons and a quiz to it.");
    }

    private void addLesson(Instructor instructor) throws LmsException {
        Course course = pickCourse(courses.coursesBy(instructor), "Course to add a lesson to");
        if (course == null) {
            return;
        }
        String title = in.readNonEmpty("Lesson title   : ");
        String content = in.readNonEmpty("Lesson content : ");
        int minutes = in.readInt("Duration (minutes, 1-600): ", 1, 600);
        courses.addLesson(instructor, course, title, content, minutes);
        store.save();
        System.out.println("Lesson added. \"" + course.getTitle() + "\" now has "
                + course.getLessons().size() + " lesson(s).");
    }

    private void addQuiz(Instructor instructor) throws LmsException {
        Course course = pickCourse(courses.coursesBy(instructor), "Course to add a quiz to");
        if (course == null) {
            return;
        }
        String title = in.readNonEmpty("Quiz title: ");
        int pass = in.readInt("Pass mark in percent (1-100): ", 1, 100);
        int count = in.readInt("Number of questions (1-20): ", 1, 20);
        Quiz quiz = new Quiz(title, pass);
        for (int i = 1; i <= count; i++) {
            System.out.println();
            System.out.println("Question " + i + " of " + count);
            int type = in.readInt("Type: 1. Multiple choice  2. True/False : ", 1, 2);
            String text = in.readNonEmpty("Question text: ");
            int marks = in.readInt("Marks (1-10): ", 1, 10);
            if (type == 1) {
                int optionCount = in.readInt("How many options (2-5): ", 2, 5);
                List<String> options = new ArrayList<String>();
                for (int o = 0; o < optionCount; o++) {
                    options.add(in.readNonEmpty("  Option " + (char) ('A' + o) + ": "));
                }
                int correct = in.readInt("Number of the correct option (1-" + optionCount + "): ", 1, optionCount);
                quiz.addQuestion(new MultipleChoiceQuestion(text, marks, options, correct - 1));
            } else {
                boolean correct = in.readYesNo("Is the statement true?");
                quiz.addQuestion(new TrueFalseQuestion(text, marks, correct));
            }
        }
        courses.addQuiz(instructor, course, quiz);
        store.save();
        System.out.println("Quiz \"" + quiz.getTitle() + "\" added (" + quiz.getTotalMarks() + " marks).");
    }

    private void showStudentProgress(Instructor instructor) {
        Course course = pickCourse(courses.coursesBy(instructor), "Course");
        if (course == null) {
            return;
        }
        List<Enrollment> list = enrollments.enrollmentsIn(course);
        if (list.isEmpty()) {
            System.out.println("No students are enrolled in this course yet.");
            return;
        }
        heading("STUDENT PROGRESS - " + course.getTitle());
        printEnrollmentTable(list, false);
    }

    private void showAtRisk(Instructor instructor) {
        Course course = pickCourse(courses.coursesBy(instructor), "Course");
        if (course == null) {
            return;
        }
        heading("AT-RISK STUDENTS - " + course.getTitle());
        List<String> risky = smart.atRiskStudents(course);
        if (risky.isEmpty()) {
            System.out.println("No student is at risk in this course.");
        }
        for (String line : risky) {
            System.out.println(" ! " + line);
        }
    }

    private void showLeaderboard(Instructor instructor) {
        Course course = pickCourse(courses.coursesBy(instructor), "Course");
        if (course == null) {
            return;
        }
        List<Enrollment> list = enrollments.leaderboard(course);
        if (list.isEmpty()) {
            System.out.println("No students are enrolled in this course yet.");
            return;
        }
        heading("LEADERBOARD - " + course.getTitle());
        printEnrollmentTable(list, true);
    }

    private void printEnrollmentTable(List<Enrollment> list, boolean ranked) {
        System.out.printf("%-5s %-6s %-24s %9s %9s %6s  %s%n",
                ranked ? "Rank" : "No.", "ID", "Student", "Progress", "Average", "Grade", "Status");
        System.out.println(LINE);
        int row = 1;
        for (Enrollment e : list) {
            String average = e.hasQuizAttempts() ? String.format("%.1f%%", e.getAverageScore()) : "-";
            String grade = e.hasQuizAttempts() ? GradeCalculator.letter(e.getAverageScore()) : "-";
            System.out.printf("%-5d %-6s %-24s %8.0f%% %9s %6s  %s%n", row++, e.getStudent().getId(),
                    e.getStudent().getName(), e.getProgressPercent(), average, grade,
                    e.isCompleted() ? "Completed" : "In progress");
        }
    }

    // =====================================================================
    //  Admin
    // =====================================================================

    private void adminMenu(Admin admin) {
        while (true) {
            heading("ADMIN DASHBOARD - " + admin.getName() + " (" + admin.getId() + ")");
            System.out.println(" 1. List all users");
            System.out.println(" 2. Add an instructor");
            System.out.println(" 3. Activate / deactivate a user");
            System.out.println(" 4. List all courses");
            System.out.println(" 5. System report");
            System.out.println(" 6. Change password");
            System.out.println(" 0. Logout");
            int choice = in.readInt("Choose: ", 0, 6);
            try {
                switch (choice) {
                    case 1: listUsers(); break;
                    case 2: addInstructor(); break;
                    case 3: toggleUser(admin); break;
                    case 4: printCourses(courses.listAll()); break;
                    case 5: systemReport(); break;
                    case 6: changePassword(admin); break;
                    default: return;
                }
            } catch (LmsException e) {
                error(e);
            }
        }
    }

    private void listUsers() {
        heading("ALL USERS");
        System.out.printf("%-5s %-22s %-28s %-10s %s%n", "ID", "Name", "Email", "Role", "Status");
        System.out.println(LINE);
        for (User u : store.getUsers()) {
            System.out.println(u);
        }
    }

    private void addInstructor() throws LmsException {
        heading("ADD INSTRUCTOR");
        String name = in.readNonEmpty("Full name      : ");
        String email = in.readNonEmpty("Email          : ");
        String password = in.readPassword("Password (min " + AuthService.MIN_PASSWORD_LENGTH + " characters): ");
        String specialization = in.readNonEmpty("Specialization : ");
        Instructor instructor = auth.registerInstructor(name, email, password, specialization);
        store.save();
        System.out.println("Instructor added with ID " + instructor.getId() + ".");
    }

    private void toggleUser(Admin admin) throws LmsException {
        listUsers();
        String id = in.readNonEmpty("User ID to activate/deactivate: ");
        User target = store.findUserById(id);
        boolean makeActive = target == null || !target.isActive();
        User changed = auth.setActive(admin, id, makeActive);
        store.save();
        System.out.println(changed.getName() + " is now " + (changed.isActive() ? "ACTIVE" : "INACTIVE") + ".");
    }

    private void systemReport() {
        heading("SYSTEM REPORT");
        int students = 0;
        int instructors = 0;
        int admins = 0;
        for (User u : store.getUsers()) {
            if (u.getRole() == Role.STUDENT) {
                students++;
            } else if (u.getRole() == Role.INSTRUCTOR) {
                instructors++;
            } else {
                admins++;
            }
        }
        List<Enrollment> all = store.getEnrollments();
        int completed = 0;
        for (Enrollment e : all) {
            if (e.isCompleted()) {
                completed++;
            }
        }
        System.out.println("Users       : " + students + " student(s), " + instructors + " instructor(s), "
                + admins + " admin(s)");
        System.out.println("Courses     : " + store.getCourses().size());
        System.out.println("Enrollments : " + all.size() + " (" + completed + " completed)");
        System.out.println();
        System.out.printf("%-5s %-38s %9s %13s %12s%n", "ID", "Course", "Students", "Avg progress", "Avg score");
        System.out.println(LINE);
        for (Course c : store.getCourses()) {
            List<Enrollment> list = enrollments.enrollmentsIn(c);
            double progress = 0;
            double score = 0;
            int scored = 0;
            for (Enrollment e : list) {
                progress += e.getProgressPercent();
                if (e.hasQuizAttempts()) {
                    score += e.getAverageScore();
                    scored++;
                }
            }
            String avgProgress = list.isEmpty() ? "-" : String.format("%.0f%%", progress / list.size());
            String avgScore = scored == 0 ? "-" : String.format("%.1f%%", score / scored);
            System.out.printf("%-5s %-38s %9d %13s %12s%n", c.getId(), c.getTitle(), list.size(),
                    avgProgress, avgScore);
        }
    }

    // =====================================================================
    //  Shared helpers
    // =====================================================================

    private void changePassword(User user) throws LmsException {
        String oldPassword = in.readPassword("Current password: ");
        String newPassword = in.readPassword("New password (min " + AuthService.MIN_PASSWORD_LENGTH + " characters): ");
        auth.changePassword(user, oldPassword, newPassword);
        store.save();
        System.out.println("Password changed.");
    }

    /** Lets the user pick an existing category or type a new one. */
    private String chooseCategory(String label) {
        List<String> categories = new ArrayList<String>();
        for (Course c : store.getCourses()) {
            boolean known = false;
            for (String existing : categories) {
                if (existing.equalsIgnoreCase(c.getCategory())) {
                    known = true;
                }
            }
            if (!known) {
                categories.add(c.getCategory());
            }
        }
        System.out.println(label + ":");
        for (int i = 0; i < categories.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + categories.get(i));
        }
        System.out.println("  " + (categories.size() + 1) + ". Other (type your own)");
        int choice = in.readInt("Choose: ", 1, categories.size() + 1);
        if (choice <= categories.size()) {
            return categories.get(choice - 1);
        }
        return in.readNonEmpty("Enter " + label.toLowerCase() + ": ");
    }

    private void printCourses(List<Course> list) {
        if (list.isEmpty()) {
            System.out.println("There are no courses yet.");
            return;
        }
        heading("COURSES");
        System.out.printf("%-5s %-38s %-16s %-12s %s%n", "ID", "Title", "Category", "Level", "Content");
        System.out.println(LINE);
        for (Course c : list) {
            System.out.println(c);
            System.out.println("      " + c.getDescription() + "  (Instructor: " + c.getInstructor().getName() + ")");
        }
    }

    /** @return the chosen course, or null if the list is empty or the user cancels */
    private Course pickCourse(List<Course> list, String label) {
        if (list.isEmpty()) {
            System.out.println("There are no courses to choose from.");
            return null;
        }
        System.out.println(label + ":");
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            System.out.println("  " + (i + 1) + ". " + c.getId() + " " + c.getTitle() + " [" + c.getLevel() + "]");
        }
        int n = in.readInt("Choose (0 to cancel): ", 0, list.size());
        return n == 0 ? null : list.get(n - 1);
    }

    /** @return one of the student's enrollments, or null if there are none or the user cancels */
    private Enrollment pickEnrollment(Student student) {
        List<Enrollment> mine = enrollments.enrollmentsOf(student);
        if (mine.isEmpty()) {
            System.out.println("You are not enrolled in any course yet. Use 'Enroll in a course' first.");
            return null;
        }
        System.out.println("Your courses:");
        for (int i = 0; i < mine.size(); i++) {
            Enrollment e = mine.get(i);
            System.out.printf("  %d. %s (%.0f%% complete)%n", i + 1, e.getCourse().getTitle(), e.getProgressPercent());
        }
        int n = in.readInt("Choose (0 to cancel): ", 0, mine.size());
        return n == 0 ? null : mine.get(n - 1);
    }

    private void heading(String title) {
        System.out.println();
        System.out.println(LINE);
        System.out.println(" " + title);
        System.out.println(LINE);
    }

    private void error(LmsException e) {
        System.out.println("Error: " + e.getMessage());
    }

    private String progressBar(double percent) {
        int filled = (int) Math.round(percent / 5.0);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < filled ? '#' : '.');
        }
        return sb.append(']').toString();
    }

    /** Prints long text wrapped at about 72 characters. */
    private void printWrapped(String text) {
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (line.length() + word.length() + 1 > 72 && line.length() > 0) {
                System.out.println(line);
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            System.out.println(line);
        }
    }
}
