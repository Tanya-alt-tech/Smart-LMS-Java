/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms.service;

import java.util.Arrays;

import lms.exception.LmsException;
import lms.model.Course;
import lms.model.Instructor;
import lms.model.Level;
import lms.model.MultipleChoiceQuestion;
import lms.model.Quiz;
import lms.model.TrueFalseQuestion;

/** Default accounts and courses created the first time the program runs. */
final class SeedData {
    private SeedData() {
    }

    static void populate(DataStore store) throws LmsException {
        AuthService auth = new AuthService(store);
        CourseService courses = new CourseService(store);

        auth.registerAdmin("System Admin", "admin@lms.com", "admin123");
        Instructor teacher = auth.registerInstructor("Dr. Anita Sharma", "instructor@lms.com",
                "teach123", "Computer Science");
        auth.registerStudent("Rohan Kapoor", "student@lms.com", "learn123", "Programming");

        // ---- Course 1 ----
        Course java = courses.createCourse(teacher, "Java Programming Basics", "Programming",
                Level.BEGINNER, "Start coding in Java: variables, control flow and methods.");
        courses.addLesson(teacher, java, "Introduction to Java",
                "Java is a high-level, object-oriented language. Source code (.java) is compiled by "
                        + "javac into bytecode (.class), which runs on the Java Virtual Machine (JVM). "
                        + "This is why the same program runs on any operating system.", 20);
        courses.addLesson(teacher, java, "Variables and Data Types",
                "Java has 8 primitive types: byte, short, int, long, float, double, char and boolean. "
                        + "Every variable must be declared with a type, e.g. int age = 20; "
                        + "String is a class, not a primitive type.", 25);
        courses.addLesson(teacher, java, "Control Flow",
                "Decisions use if / else and switch. Repetition uses for, while and do-while loops. "
                        + "A do-while loop always runs its body at least once; break leaves a loop "
                        + "and continue skips to the next iteration.", 30);
        Quiz javaQuiz = new Quiz("Java Basics Quiz", 50);
        javaQuiz.addQuestion(new MultipleChoiceQuestion("Which component runs Java bytecode?", 2,
                Arrays.asList("javac", "JVM", "JDK installer", "Text editor"), 1));
        javaQuiz.addQuestion(new MultipleChoiceQuestion("Which of these is NOT a primitive type in Java?", 2,
                Arrays.asList("int", "boolean", "String", "char"), 2));
        javaQuiz.addQuestion(new TrueFalseQuestion("A do-while loop always executes its body at least once.", 1, true));
        javaQuiz.addQuestion(new MultipleChoiceQuestion("How many primitive data types does Java have?", 2,
                Arrays.asList("6", "7", "8", "9"), 2));
        javaQuiz.addQuestion(new TrueFalseQuestion("Java source code is compiled directly into machine code "
                + "for one specific operating system.", 1, false));
        courses.addQuiz(teacher, java, javaQuiz);

        // ---- Course 2 ----
        Course oop = courses.createCourse(teacher, "Object-Oriented Programming in Java", "Programming",
                Level.INTERMEDIATE, "Classes, inheritance, polymorphism, abstraction and interfaces.");
        courses.addLesson(teacher, oop, "Classes and Objects",
                "A class is a blueprint; an object is an instance of a class created with new. "
                        + "Encapsulation means keeping fields private and exposing them through methods.", 30);
        courses.addLesson(teacher, oop, "Inheritance and Polymorphism",
                "A subclass uses extends to inherit from one superclass. Overriding a method lets a "
                        + "subclass give its own behaviour; the method that runs is chosen at runtime "
                        + "from the actual object type (runtime polymorphism).", 35);
        courses.addLesson(teacher, oop, "Abstract Classes and Interfaces",
                "An abstract class cannot be instantiated and may contain abstract methods. "
                        + "An interface is a contract; a class can implement many interfaces "
                        + "but extend only one class.", 35);
        Quiz oopQuiz = new Quiz("OOP Concepts Quiz", 60);
        oopQuiz.addQuestion(new MultipleChoiceQuestion("Which keyword is used to inherit from a class?", 2,
                Arrays.asList("implements", "extends", "inherits", "super"), 1));
        oopQuiz.addQuestion(new MultipleChoiceQuestion("Hiding fields behind private access and public "
                + "methods is called:", 2,
                Arrays.asList("Polymorphism", "Inheritance", "Encapsulation", "Compilation"), 2));
        oopQuiz.addQuestion(new TrueFalseQuestion("An abstract class can be instantiated with new.", 1, false));
        oopQuiz.addQuestion(new TrueFalseQuestion("A Java class can implement more than one interface.", 1, true));
        oopQuiz.addQuestion(new MultipleChoiceQuestion("Choosing the overridden method at runtime based on "
                + "the object's actual type is:", 2,
                Arrays.asList("Method overloading", "Runtime polymorphism", "Encapsulation", "Type casting"), 1));
        courses.addQuiz(teacher, oop, oopQuiz);

        // ---- Course 3 ----
        Course web = courses.createCourse(teacher, "Web Design Fundamentals", "Web Development",
                Level.BEGINNER, "Build and style web pages with HTML and CSS.");
        courses.addLesson(teacher, web, "HTML Structure",
                "HTML (HyperText Markup Language) describes the structure of a page using elements "
                        + "such as headings (h1-h6), paragraphs (p), links (a) and images (img).", 20);
        courses.addLesson(teacher, web, "Styling with CSS",
                "CSS (Cascading Style Sheets) controls presentation: colours, fonts and layout. "
                        + "Rules consist of a selector and declarations, e.g. p { color: blue; }", 25);
        courses.addLesson(teacher, web, "Responsive Design",
                "Responsive pages adapt to the screen size using flexible layouts and media queries, "
                        + "e.g. @media (max-width: 600px) { ... }", 25);
        Quiz webQuiz = new Quiz("Web Basics Quiz", 50);
        webQuiz.addQuestion(new MultipleChoiceQuestion("What does HTML stand for?", 2,
                Arrays.asList("HyperText Markup Language", "HighText Machine Language",
                        "Hyperlink Text Mode Language", "Home Tool Markup Language"), 0));
        webQuiz.addQuestion(new MultipleChoiceQuestion("Which HTML element creates a hyperlink?", 2,
                Arrays.asList("<link>", "<a>", "<href>", "<p>"), 1));
        webQuiz.addQuestion(new TrueFalseQuestion("CSS is used to control the presentation of a web page.", 1, true));
        webQuiz.addQuestion(new MultipleChoiceQuestion("Which CSS feature applies styles only at certain "
                + "screen sizes?", 2,
                Arrays.asList("Selectors", "Media queries", "Comments", "Hyperlinks"), 1));
        courses.addQuiz(teacher, web, webQuiz);

        // ---- Course 4 ----
        Course sec = courses.createCourse(teacher, "Introduction to Cybersecurity", "Cybersecurity",
                Level.BEGINNER, "Core security principles, common threats and safe passwords.");
        courses.addLesson(teacher, sec, "The CIA Triad",
                "Security aims to protect Confidentiality (only authorised people can read data), "
                        + "Integrity (data is not altered improperly) and Availability "
                        + "(systems work when needed).", 20);
        courses.addLesson(teacher, sec, "Common Threats",
                "Phishing tricks people into revealing information through fake messages. "
                        + "Malware is malicious software such as viruses and ransomware. "
                        + "A denial-of-service attack floods a system so real users cannot use it.", 25);
        courses.addLesson(teacher, sec, "Passwords and Hashing",
                "Passwords should never be stored as plain text. Systems store a hash, a one-way "
                        + "function of the password, usually combined with a random salt so that "
                        + "identical passwords produce different hashes. This LMS does exactly that.", 25);
        Quiz secQuiz = new Quiz("Security Basics Quiz", 50);
        secQuiz.addQuestion(new MultipleChoiceQuestion("In the CIA triad, the letter A stands for:", 2,
                Arrays.asList("Authentication", "Availability", "Authorisation", "Accounting"), 1));
        secQuiz.addQuestion(new MultipleChoiceQuestion("A fake email asking you to 'verify your password' "
                + "is an example of:", 2,
                Arrays.asList("Phishing", "Hashing", "Encryption", "A firewall"), 0));
        secQuiz.addQuestion(new TrueFalseQuestion("Storing passwords in plain text is a safe practice.", 1, false));
        secQuiz.addQuestion(new TrueFalseQuestion("A salt makes identical passwords produce different hashes.", 1, true));
        courses.addQuiz(teacher, sec, secQuiz);
    }
}
