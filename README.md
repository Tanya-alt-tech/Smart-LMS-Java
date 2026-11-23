# Smart Learning Management System (LMS)

**Mini project - Object-Oriented Programming using Java (Advanced level)**  
**Submitted by:** Tanyaradzwa Masimo  
**University:** Manav Rachna

A menu-driven console application in which **students** enroll in courses, study
lessons, take auto-graded quizzes and earn certificates; **instructors** build
courses and monitor their students; and an **admin** manages accounts and views
system reports. All data is saved to a file, so it is still there on the next run.

It uses only the standard Java library (no external JARs) and works on Java 8 or newer.

## How to run

Open a terminal (Command Prompt on Windows) inside the `SmartLMS` folder.

**Windows:** double-click `run.bat`, or type:

    javac -d out -sourcepath src src\lms\Main.java
    java -cp out lms.Main

**Linux / macOS:** `./run.sh`, or type:

    javac -d out -sourcepath src src/lms/Main.java
    java -cp out lms.Main

**In VS Code:** install the "Extension Pack for Java" extension, choose File > Open Folder and
open the `SmartLMS` folder, then press F5 (or Run > Start Debugging) and pick
**Run Smart LMS**. The program runs in the Terminal panel at the bottom, where you
type your choices.

**In another IDE (Eclipse / IntelliJ IDEA / NetBeans):** create a Java
project, copy the `lms` folder from `src` into the project's `src` folder, and
run `lms.Main`.

### Default accounts

| Role       | Email               | Password  |
|------------|---------------------|-----------|
| Admin      | admin@lms.com       | admin123  |
| Instructor | instructor@lms.com  | teach123  |
| Student    | student@lms.com     | learn123  |

Four default courses (Java Basics, OOP in Java, Web Design, Cybersecurity) are
created on the first run. To reset everything, delete `lms_data.ser`.

### Suggested 3-minute walkthrough

1. Log in as the student. Open **Smart recommendations** (Java Basics is ranked first).
2. **Enroll** in Java Programming Basics, **study** the three lessons, **take the quiz**.
   Answer key: `B, C, T, C, F`. Give one wrong answer first to show the review,
   the "retake" advice and that the best attempt is kept.
3. Open **Report card**, then **Get certificate** (also saved in `certificates/`).
4. Open **Smart recommendations** again: the Intermediate OOP course is now first.
5. Log in as the instructor: **Student progress**, **Leaderboard**, **At-risk students**,
   and create a course with a lesson and a quiz.
6. Log in as the admin: **System report**, and deactivate a user.
7. Exit and run again to show that the data was saved.

## Automated test

`LmsSelfTest` checks every business rule (98 checks) without needing JUnit:

    javac -d out -sourcepath src src/lms/test/LmsSelfTest.java
    java -cp out lms.test.LmsSelfTest

(on Windows use `src\lms\test\LmsSelfTest.java`, or run `test.bat`). The last
line should read `Result: 98 passed, 0 failed.`

## Features

**Student**
- Register, log in, change password
- Browse and search courses; enroll
- Study lessons and mark them complete; progress bar per course
- Take quizzes (multiple choice and true/false), unlimited retakes, best score counts
- Report card with letter grades; certificate once a course is completed

**Instructor**
- Create courses; add lessons and quizzes (only to their own courses)
- View student progress and a leaderboard per course

**Admin**
- List users, add instructors, activate/deactivate accounts
- System report: users, courses, enrollments, average progress and score per course

**What makes it "smart"** (`RecommendationService`)
1. **Course recommendations** - every course the student has not joined is scored:
   +3 if it matches the student's interest, +2 if the student already studies that
   category, +2 if the level is exactly right, -2 if it is too advanced. The right
   level starts at Beginner and moves up one step each time the student completes
   a course in that category with an average of 60% or more. Each recommendation
   is shown with its reasons.
2. **Next-step guidance** - tells the student exactly what to do next in each course.
3. **Weak-area detection** - lists quizzes whose best score is below 60%.
4. **At-risk alerts** - shows the instructor students with progress below 25% or
   a quiz average below 50%.

**Grading rules**
- Quiz percentage = marks scored / total marks x 100; a quiz is passed at or above its pass mark.
- Course progress = (lessons completed + quizzes passed) / (lessons + quizzes) x 100.
- Course average = mean of the best percentage in each attempted quiz.
- Grades: A+ 90 and above, A 80-89, B 70-79, C 60-69, D 50-59, F below 50.

## OOP concepts used

| Concept | Where |
|---|---|
| Classes and objects | every file; e.g. `Course`, `Lesson`, `Quiz` |
| Encapsulation | all fields `private`, accessed through methods; lists returned read-only |
| Inheritance | `Student`, `Instructor`, `Admin` extend `User`; `MultipleChoiceQuestion`, `TrueFalseQuestion` extend `Question`; all exceptions extend `LmsException` |
| Abstraction | abstract classes `User` and `Question`; interface `Trackable` |
| Polymorphism (overriding) | `Quiz.grade()` calls `question.isCorrect()` without knowing the question type; `user.describe()` and `user.getRole()` |
| Interface | `Trackable` implemented by `Enrollment`; `Serializable`; `Comparator` |
| Composition / aggregation | a `Course` has `Lesson`s and `Quiz`zes; a `Quiz` has `Question`s; an `Enrollment` links a `Student` and a `Course` |
| Constructors, `super`, `this` | subclass constructors call `super(...)` |
| `static` and `final` | utility classes `PasswordUtil`, `GradeCalculator`; constants |
| Enums | `Role`, `Level` |
| Exception handling | custom checked exceptions; `try`/`catch` in every menu; try-with-resources for files |
| Collections and generics | `ArrayList`, `LinkedHashMap`, `TreeSet`, `TreeMap` |
| File handling | object serialization in `DataStore`; certificate text files |
| Packages | `model`, `service`, `exception`, `util`, `ui`, `test` |
| Nested and anonymous classes | `RecommendationService.Recommendation`; anonymous `Comparator`s |

## Class diagram

    <<abstract>> User                         <<abstract>> Question
    - id, name, email, passwordHash          - text, marks
    + getRole(), describe()                  + isCorrect(answer), getPrompt()
        ^          ^          ^                   ^                  ^
        |          |          |                   |                  |
     Student   Instructor   Admin     MultipleChoiceQuestion   TrueFalseQuestion
     - interest - specialization       - options, correctIndex  - correctAnswer

    Instructor 1 ----- * Course 1 ----- * Lesson
                         Course 1 ----- * Quiz 1 ----- * Question
                                          Quiz --grades--> QuizResult

    Student 1 ----- * Enrollment * ----- 1 Course
                      Enrollment implements <<interface>> Trackable
                      - completed lessons, quiz attempts (QuizResult)
                      + getProgressPercent(), isCompleted(), getAverageScore()

    ConsoleUI --> AuthService, CourseService, EnrollmentService, RecommendationService
                  (all services) --> DataStore --saves--> lms_data.ser

## Project structure

    src/lms/
      Main.java                      entry point
      model/                         the data classes
        User (abstract) -> Student, Instructor, Admin
        Question (abstract) -> MultipleChoiceQuestion, TrueFalseQuestion
        Course, Lesson, Quiz, QuizResult, Enrollment
        Trackable (interface), Role, Level (enums)
      service/                       the business rules
        DataStore                    holds and saves all data
        AuthService                  register, login, passwords, activation
        CourseService                courses, lessons, quizzes, search
        EnrollmentService            enrolling, progress, grading, leaderboard, certificate
        RecommendationService        the smart features
        SeedData                     default accounts and courses
      exception/                     LmsException and its four subclasses
      util/                          PasswordUtil, GradeCalculator, InputHelper
      ui/ConsoleUI.java              all menus (input and output only)
      test/LmsSelfTest.java          automated checks

The design is layered: `ui` talks to `service`, `service` works on `model`.
The menus contain no rules, which is why the rules can be tested automatically.

## Security and robustness

- Passwords are never stored: only a salted SHA-256 hash is kept, and they are
  hidden while typed.
- Login gives the same message for a wrong email and a wrong password.
- Every input is validated; wrong menu numbers, letters instead of numbers and
  empty text are asked for again instead of crashing the program.
- If the data file is damaged it is kept aside as `lms_data.ser.corrupt` and the
  program starts again with the default accounts and courses.

## Limitations

- Console interface only (no GUI), single user at a time.
- Passwords are hidden while typed in a terminal (Command Prompt or the VS Code
  terminal); in IDE consoles that have no terminal they are shown as typed.
- Data is stored in a local file rather than a database.
