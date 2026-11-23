/*
 * Smart Learning Management System (LMS)
 * Mini project - Object-Oriented Programming using Java
 * Author: Tanyaradzwa Masimo, Manav Rachna
 */
package lms;

import java.io.File;

import lms.service.DataStore;
import lms.ui.ConsoleUI;

/**
 * Smart Learning Management System - entry point.
 *
 * Compile:  javac -d out -sourcepath src src/lms/Main.java
 * Run:      java -cp out lms.Main
 */
public class Main {
    public static void main(String[] args) {
        DataStore store = DataStore.load(new File("lms_data.ser"));
        new ConsoleUI(store, System.in).run();
    }
}
