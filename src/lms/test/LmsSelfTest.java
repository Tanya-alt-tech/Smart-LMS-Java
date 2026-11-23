/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lms.exception.AuthenticationException;
import lms.exception.DuplicateEntryException;
import lms.exception.LmsException;
import lms.exception.NotFoundException;
import lms.exception.ValidationException;
import lms.model.Admin;
import lms.model.Course;
import lms.model.Enrollment;
import lms.model.Instructor;
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
import lms.util.PasswordUtil;

/**
 * Automated checks of every business rule, written in plain Java
 * (no JUnit needed).
 *
 * Compile:  javac -d out -sourcepath src src/lms/test/LmsSelfTest.java
 * Run:      java -cp out lms.test.LmsSelfTest
 */
public class LmsSelfTest {
    private static int passed;
    private static int failed;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }

    /** A piece of code that is expected to throw. */
    private interface Action {
        void run() throws Exception;
    }

    private static void expect(String name, Class<? extends Exception> type, Action action) {
        try {
            action.run();
            check(name + " (nothing was thrown)", false);
        } catch (Exception e) {
            check(name, type.isInstance(e));
        }
    }

    public static void main(String[] args) throws Exception {
        final DataStore store = DataStore.inMemory();
        final AuthService auth = new AuthService(store);
        final CourseService courses = new CourseService(store);
        final EnrollmentService enrollments = new EnrollmentService(store);
        final RecommendationService smart = new RecommendationService(store, enrollments);

        System.out.println("== Passwords ==");
        String salt = PasswordUtil.newSalt();
        check("same password + same salt gives same hash",
                PasswordUtil.hash("secret1", salt).equals(PasswordUtil.hash("secret1", salt)));
        check("different salts give different hashes",
                !PasswordUtil.hash("secret1", salt).equals(PasswordUtil.hash("secret1", PasswordUtil.newSalt())));
        check("hash is 64 hex characters (SHA-256)", PasswordUtil.hash("x", salt).matches("[0-9a-f]{64}"));

        System.out.println("== Registration and login ==");
        final Admin admin = auth.registerAdmin("Root", "root@lms.com", "rootpass");
        final Instructor teacher = auth.registerInstructor("Teacher One", "T1@LMS.com", "teach123", "CS");
        final Instructor other = auth.registerInstructor("Teacher Two", "t2@lms.com", "teach123", "Maths");
        final Student asha = auth.registerStudent("Asha", "asha@mail.com", "asha123", "Programming");
        final Student ravi = auth.registerStudent("Ravi", "ravi@mail.com", "ravi123", "Design");
        check("IDs carry the role prefix",
                admin.getId().equals("A001") && teacher.getId().equals("I002") && asha.getId().equals("S004"));
        check("roles are reported polymorphically",
                admin.getRole() == Role.ADMIN && teacher.getRole() == Role.INSTRUCTOR && asha.getRole() == Role.STUDENT);
        check("email is stored in lower case", teacher.getEmail().equals("t1@lms.com"));
        check("login succeeds with correct password", auth.login("asha@mail.com", "asha123") == asha);
        check("login ignores email case and spaces", auth.login("  ASHA@mail.com ", "asha123") == asha);
        expect("login fails with wrong password", AuthenticationException.class,
                new Action() { public void run() throws Exception { auth.login("asha@mail.com", "wrong"); } });
        expect("login fails for unknown email", AuthenticationException.class,
                new Action() { public void run() throws Exception { auth.login("nobody@mail.com", "asha123"); } });
        expect("duplicate email is rejected (any case)", DuplicateEntryException.class,
                new Action() { public void run() throws Exception { auth.registerStudent("X", "ASHA@mail.com", "abcdef", "Y"); } });
        expect("invalid email is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { auth.registerStudent("X", "not-an-email", "abcdef", "Y"); } });
        expect("short password is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { auth.registerStudent("X", "x@mail.com", "abc", "Y"); } });
        expect("blank name is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { auth.registerStudent("   ", "x@mail.com", "abcdef", "Y"); } });
        check("rejected registrations create no account", store.getUsers().size() == 5);

        System.out.println("== Account management ==");
        expect("change password needs the current password", AuthenticationException.class,
                new Action() { public void run() throws Exception { auth.changePassword(ravi, "wrong", "newpass1"); } });
        expect("new password must be long enough", ValidationException.class,
                new Action() { public void run() throws Exception { auth.changePassword(ravi, "ravi123", "abc"); } });
        auth.changePassword(ravi, "ravi123", "ravi4567");
        check("new password works after change", auth.login("ravi@mail.com", "ravi4567") == ravi);
        expect("old password stops working", AuthenticationException.class,
                new Action() { public void run() throws Exception { auth.login("ravi@mail.com", "ravi123"); } });
        auth.setActive(admin, ravi.getId(), false);
        expect("deactivated user cannot log in", AuthenticationException.class,
                new Action() { public void run() throws Exception { auth.login("ravi@mail.com", "ravi4567"); } });
        auth.setActive(admin, ravi.getId().toLowerCase(), true);
        check("reactivated user can log in", auth.login("ravi@mail.com", "ravi4567") == ravi);
        expect("admin cannot deactivate own account", ValidationException.class,
                new Action() { public void run() throws Exception { auth.setActive(admin, admin.getId(), false); } });
        expect("unknown user ID is reported", NotFoundException.class,
                new Action() { public void run() throws Exception { auth.setActive(admin, "S999", false); } });

        System.out.println("== Questions and quizzes ==");
        Question mcq = new MultipleChoiceQuestion("2 + 2 = ?", 2, Arrays.asList("3", "4", "5"), 1);
        Question tf = new TrueFalseQuestion("Java is object-oriented.", 1, true);
        check("MCQ accepts the right letter in any case", mcq.isCorrect("B") && mcq.isCorrect(" b "));
        check("MCQ accepts the option number", mcq.isCorrect("2"));
        check("MCQ marks a wrong option as wrong", !mcq.isCorrect("A") && !mcq.isCorrect("3"));
        check("MCQ rejects answers outside its options",
                !mcq.isValidAnswer("D") && !mcq.isValidAnswer("4") && !mcq.isValidAnswer("") && !mcq.isValidAnswer("AB"));
        check("MCQ answer hint matches option count", mcq.getAnswerHint().equals("A-C"));
        check("True/False accepts T, true, F, false",
                tf.isCorrect("T") && tf.isCorrect("true") && !tf.isCorrect("F") && !tf.isCorrect("FALSE"));
        check("True/False rejects other text", !tf.isValidAnswer("yes") && !tf.isValidAnswer(""));
        expect("MCQ with one option is rejected", IllegalArgumentException.class,
                new Action() { public void run() { new MultipleChoiceQuestion("q", 1, Arrays.asList("only"), 0); } });
        expect("MCQ with out-of-range answer is rejected", IllegalArgumentException.class,
                new Action() { public void run() { new MultipleChoiceQuestion("q", 1, Arrays.asList("a", "b"), 2); } });

        System.out.println("== Courses ==");
        final Course basics = courses.createCourse(teacher, "Java Basics", "Programming", Level.BEGINNER, "Intro");
        final Course oop = courses.createCourse(teacher, "Java OOP", "Programming", Level.INTERMEDIATE, "OOP");
        final Course expert = courses.createCourse(teacher, "Java Expert", "Programming", Level.ADVANCED, "Deep");
        final Course art = courses.createCourse(other, "Colour Theory", "Design", Level.BEGINNER, "Colours");
        courses.addLesson(teacher, basics, "L1", "content", 10);
        courses.addLesson(teacher, basics, "L2", "content", 20);
        final Quiz quiz = new Quiz("Q1", 60);
        quiz.addQuestion(mcq);                                                        // 2 marks
        quiz.addQuestion(tf);                                                         // 1 mark
        quiz.addQuestion(new TrueFalseQuestion("int is a class.", 2, false));         // 2 marks
        courses.addQuiz(teacher, basics, quiz);
        courses.addLesson(teacher, oop, "L1", "content", 30);
        check("course IDs are sequential", basics.getId().equals("C001") && art.getId().equals("C004"));
        check("total duration adds up", basics.getTotalDurationMinutes() == 30);
        check("quiz total marks add up", quiz.getTotalMarks() == 5);
        check("search finds by title, any case", courses.search("OOP").size() == 1 && courses.search("java").size() == 3);
        check("search finds by category and level",
                courses.search("design").size() == 1 && courses.search("beginner").size() == 2);
        check("blank or unmatched search returns nothing", courses.search("  ").isEmpty() && courses.search("zzz").isEmpty());
        check("courses are listed per instructor",
                courses.coursesBy(teacher).size() == 3 && courses.coursesBy(other).size() == 1);
        check("course lookup ignores case", courses.getCourse("c002") == oop);
        expect("unknown course ID is reported", NotFoundException.class,
                new Action() { public void run() throws Exception { courses.getCourse("C999"); } });
        expect("only the owner can add a lesson", ValidationException.class,
                new Action() { public void run() throws Exception { courses.addLesson(other, basics, "Hack", "x", 5); } });
        expect("empty quiz is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { courses.addQuiz(teacher, basics, new Quiz("Empty", 50)); } });
        expect("blank course title is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { courses.createCourse(teacher, " ", "C", Level.BEGINNER, "d"); } });
        expect("zero-minute lesson is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { courses.addLesson(teacher, basics, "L", "c", 0); } });
        check("rejected changes leave the course untouched",
                basics.getLessons().size() == 2 && basics.getQuizzes().size() == 1);

        System.out.println("== Recommendations for a new student ==");
        List<Recommendation> recs = smart.recommendCourses(asha, 10);
        check("best match is the beginner course in the student's interest",
                recs.get(0).getCourse() == basics && recs.get(0).getScore() == 5);
        check("intermediate course ranks low before the beginner one is done", scoreOf(recs, oop) == 1);
        check("unrelated beginner course is still suggested", scoreOf(recs, art) == 2);
        check("recommendations are sorted best first", isSorted(recs));
        check("limit is respected", smart.recommendCourses(asha, 1).size() == 1);

        System.out.println("== Enrollment and progress ==");
        final Enrollment e = enrollments.enroll(asha, basics);
        expect("enrolling twice is rejected", DuplicateEntryException.class,
                new Action() { public void run() throws Exception { enrollments.enroll(asha, basics); } });
        expect("unenrolled course is reported", NotFoundException.class,
                new Action() { public void run() throws Exception { enrollments.get(asha, art); } });
        check("new enrollment starts at 0%", near(e.getProgressPercent(), 0) && !e.isCompleted());
        check("enrolled course is no longer recommended", scoreOf(smart.recommendCourses(asha, 10), basics) == -99);
        check("next step is the first lesson", smart.nextStep(e).startsWith("Study Lesson 1"));
        check("completing a lesson returns true", enrollments.completeLesson(e, 0));
        check("completing it again returns false", !enrollments.completeLesson(e, 0));
        check("1 of 3 items done = 33.3%", near(e.getProgressPercent(), 100.0 / 3));
        expect("non-existent lesson is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { enrollments.completeLesson(e, 2); } });
        expect("negative lesson number is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { enrollments.completeLesson(e, -1); } });
        enrollments.completeLesson(e, 1);
        check("next step is then the quiz", smart.nextStep(e).startsWith("Attempt the quiz"));

        System.out.println("== Quiz grading ==");
        expect("wrong number of answers is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { enrollments.submitQuiz(e, 0, Arrays.asList("A")); } });
        expect("non-existent quiz is rejected", ValidationException.class,
                new Action() { public void run() throws Exception { enrollments.submitQuiz(e, 1, Arrays.asList("A", "T", "F")); } });
        check("rejected submissions are not recorded", !e.hasQuizAttempts());
        QuizResult low = enrollments.submitQuiz(e, 0, Arrays.asList("A", "T", "T"));   // only Q2 right: 1/5
        check("score 1/5 = 20%", low.getScore() == 1 && low.getTotalMarks() == 5 && near(low.getPercent(), 20));
        check("20% does not pass a 60% quiz", !e.isQuizPassed(0) && !e.isCompleted());
        check("failed quiz does not add progress", near(e.getProgressPercent(), 200.0 / 3));
        check("next step is to retake the quiz", smart.nextStep(e).startsWith("Revise and retake"));
        check("weak area is reported", smart.weakAreas(asha).size() == 1);
        check("student with low average is at risk", smart.atRiskStudents(basics).size() == 1);
        QuizResult full = enrollments.submitQuiz(e, 0, Arrays.asList("b", "true", "f"));
        check("score 5/5 = 100%", full.getScore() == 5 && near(full.getPercent(), 100));
        enrollments.submitQuiz(e, 0, Arrays.asList("B", "F", "F"));                    // 4/5 = 80%
        check("all three attempts are kept", e.getAttempts(0).size() == 3);
        check("best attempt is used, not the latest", near(e.getBestResult(0).getPercent(), 100));
        check("course is now 100% complete", near(e.getProgressPercent(), 100) && e.isCompleted());
        check("average score is 100%", near(e.getAverageScore(), 100));
        check("weak area is cleared", smart.weakAreas(asha).isEmpty());
        check("completed student is not at risk", smart.atRiskStudents(basics).isEmpty());
        check("next step says completed", smart.nextStep(e).startsWith("Course completed"));

        System.out.println("== Pass mark boundary ==");
        final Enrollment r = enrollments.enroll(ravi, basics);
        QuizResult three = enrollments.submitQuiz(r, 0, Arrays.asList("B", "T", "T")); // 3/5 = exactly 60%
        check("exactly the pass mark counts as passed", near(three.getPercent(), 60) && r.isQuizPassed(0));

        System.out.println("== Grades ==");
        check("grade boundaries",
                GradeCalculator.letter(100).equals("A+") && GradeCalculator.letter(90).equals("A+")
                        && GradeCalculator.letter(89.99).equals("A") && GradeCalculator.letter(80).equals("A")
                        && GradeCalculator.letter(79.99).equals("B") && GradeCalculator.letter(70).equals("B")
                        && GradeCalculator.letter(69.99).equals("C") && GradeCalculator.letter(60).equals("C")
                        && GradeCalculator.letter(59.99).equals("D") && GradeCalculator.letter(50).equals("D")
                        && GradeCalculator.letter(49.99).equals("F") && GradeCalculator.letter(0).equals("F"));

        System.out.println("== Recommendations adapt after completion ==");
        recs = smart.recommendCourses(asha, 10);
        check("intermediate course becomes the top pick", recs.get(0).getCourse() == oop && recs.get(0).getScore() == 7);
        check("advanced course is held back", scoreOf(recs, expert) == 3 && recs.indexOf(find(recs, expert)) > 0);

        System.out.println("== Leaderboard, at-risk and certificate ==");
        List<Enrollment> board = enrollments.leaderboard(basics);
        check("leaderboard ranks the higher average first",
                board.size() == 2 && board.get(0).getStudent() == asha && board.get(1).getStudent() == ravi);
        final Enrollment idle = enrollments.enroll(ravi, oop);
        check("student with no progress is flagged at risk",
                smart.atRiskStudents(oop).size() == 1 && smart.atRiskStudents(oop).get(0).contains("Ravi"));
        String certificate = enrollments.buildCertificate(e);
        check("certificate names student, course and grade",
                certificate.contains("Asha") && certificate.contains("Java Basics")
                        && certificate.contains("A+") && certificate.contains("CERT-C001-S004"));
        expect("certificate is refused for an unfinished course", ValidationException.class,
                new Action() { public void run() throws Exception { enrollments.buildCertificate(idle); } });

        System.out.println("== Empty course ==");
        Enrollment empty = enrollments.enroll(asha, expert);
        check("course with no content is 0% and never completed",
                near(empty.getProgressPercent(), 0) && !empty.isCompleted());
        check("next step explains there is no content", smart.nextStep(empty).startsWith("No content yet"));

        System.out.println("== Saving and loading ==");
        File file = File.createTempFile("lms-selftest", ".ser");
        file.delete();
        DataStore first = DataStore.load(file);
        check("first run creates the data file with the default data",
                file.exists() && first.getUsers().size() == 3 && first.getCourses().size() == 4);
        AuthService auth1 = new AuthService(first);
        EnrollmentService enr1 = new EnrollmentService(first);
        Student rohan = (Student) auth1.login("student@lms.com", "learn123");
        Course c1 = first.findCourseById("C001");
        Enrollment saved = enr1.enroll(rohan, c1);
        enr1.completeLesson(saved, 0);
        enr1.submitQuiz(saved, 0, Arrays.asList("B", "C", "T", "C", "F"));
        check("default quiz answer key gives full marks", near(saved.getBestResult(0).getPercent(), 100));
        Student added = auth1.registerStudent("Meera", "meera@mail.com", "meera123", "Cybersecurity");
        first.save();

        DataStore second = DataStore.load(file);
        AuthService auth2 = new AuthService(second);
        EnrollmentService enr2 = new EnrollmentService(second);
        User reloaded = auth2.login("student@lms.com", "learn123");
        check("users and passwords survive a restart",
                second.getUsers().size() == 4 && auth2.login("meera@mail.com", "meera123").getId().equals(added.getId()));
        List<Enrollment> again = enr2.enrollmentsOf((Student) reloaded);
        check("progress and quiz results survive a restart",
                again.size() == 1 && again.get(0).isLessonComplete(0) && !again.get(0).isLessonComplete(1)
                        && near(again.get(0).getBestResult(0).getPercent(), 100));
        check("loaded objects stay linked (same Course instance)",
                again.get(0).getCourse() == second.findCourseById("C001"));
        Student next = auth2.registerStudent("New", "new@mail.com", "newpass", "Programming");
        check("ID counter continues after a restart", next.getId().equals("S005"));
        checkDefaultQuizzes(second);

        java.io.FileOutputStream junk = new java.io.FileOutputStream(file);
        junk.write("this is not a valid data file".getBytes("UTF-8"));
        junk.close();
        DataStore recovered = DataStore.load(file);
        File corrupt = new File(file.getPath() + ".corrupt");
        check("corrupt data file is set aside and default data restored",
                recovered.getUsers().size() == 3 && corrupt.exists() && recovered.getLoadNote().startsWith("Warning"));
        check("recovered store can be loaded again", DataStore.load(file).getCourses().size() == 4);
        file.delete();
        corrupt.delete();

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Every default quiz must be answerable and every question must have exactly one accepted key. */
    private static void checkDefaultQuizzes(DataStore store) {
        boolean ok = store.getCourses().size() == 4;
        for (Course c : store.getCourses()) {
            ok = ok && c.getLessons().size() == 3 && c.getQuizzes().size() == 1;
            for (Quiz q : c.getQuizzes()) {
                for (Question question : q.getQuestions()) {
                    int accepted = 0;
                    for (String candidate : new String[] {"A", "B", "C", "D", "E", "T", "F"}) {
                        if (question.isValidAnswer(candidate) && question.isCorrect(candidate)) {
                            accepted++;
                        }
                    }
                    ok = ok && accepted == 1;
                }
            }
        }
        check("every default question has exactly one correct answer", ok);
    }

    private static Recommendation find(List<Recommendation> recs, Course course) {
        for (Recommendation r : recs) {
            if (r.getCourse() == course) {
                return r;
            }
        }
        return null;
    }

    /** @return the score of the course, or -99 if it is not recommended */
    private static int scoreOf(List<Recommendation> recs, Course course) {
        Recommendation r = find(recs, course);
        return r == null ? -99 : r.getScore();
    }

    private static boolean isSorted(List<Recommendation> recs) {
        for (int i = 1; i < recs.size(); i++) {
            if (recs.get(i - 1).getScore() < recs.get(i).getScore()) {
                return false;
            }
        }
        return true;
    }
}
