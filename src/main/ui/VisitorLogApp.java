package ui;

import model.VisitorEntry;
import model.VisitorLog;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;


// A console-based visitor log application that allows the user to add, view, and manage visitor entries
public class VisitorLogApp {
    private static final String JSON_STORE = "./data/visitorlog.json";
    private VisitorLog log;
    private Scanner scanner;
    private boolean isProgramRunning;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;

    // EFFECTS: creates and runs the visitor log console UI application
    public VisitorLogApp() {
        init();

        printDivider();
        System.out.println("Welcome to the Visitor Log App!");
        printDivider();

        while (this.isProgramRunning) {
            handleMenu();
        }
    }

    // MODIFIES: this
    // EFFECTS: initializes the visitor log application with starting values
    public void init() {
        this.log = new VisitorLog();
        this.scanner = new Scanner(System.in);
        this.isProgramRunning = true;
        this.jsonWriter = new JsonWriter(JSON_STORE);
        this.jsonReader = new JsonReader(JSON_STORE);
    }

    // MODIFIES: this
    // EFFECTS: displays the main menu, reads user input, and processes the selected
    // command
    public void handleMenu() {
        displayMenu();
        String input = this.scanner.nextLine();
        processMenuCommands(input);
    }

    // EFFECTS: displays the main menu options
    public void displayMenu() {
        System.out.println("Please select an option:\n");
        System.out.println("a: Add a new visitor entry");
        System.out.println("v: View all visitor entries");
        System.out.println("c: View currently signed-in visitors");
        System.out.println("s: Sign out a visitor");
        System.out.println("m: Show most frequent visitor");
        System.out.println("r: Show most frequently visited resident");
        System.out.println("t: Show average visit duration");
        System.out.println("w: Save visitor log to file");
        System.out.println("l: Load visitor log from file");
        System.out.println("q: Quit");
        printDivider();
    }

    @SuppressWarnings("methodlength")
    // MODIFIES: this
    // EFFECTS: processes the user's menu choice
    public void processMenuCommands(String input) {
        printDivider();
        switch (input) {
            case "a":
                addNewVisitor();
                break;
            case "v":
                viewAllVisitors();
                break;
            case "c":
                viewActiveVisitors();
                break;
            case "s":
                signOutVisitor();
                break;
            case "m":
                showMostFrequentVisitor();
                break;
            case "r":
                showMostVisitedResident();
                break;
            case "t":
                showAverageVisitDuration();
                break;
            case "w":
                saveVisitorLog();
                break;
            case "l":
                loadVisitorLog();
                break;
            case "q":
                quitApplication();
                break;
            default:
                System.out.println("Invalid. Try again.");
        }
        printDivider();
    }

    // MODIFIES: this
    // EFFECTS: prompts the user for visitor and resident info and adds a new entry
    public void addNewVisitor() {
        System.out.println("Enter visitor name:");
        String visitor = this.scanner.nextLine();

        System.out.println("Enter resident name:");
        String resident = this.scanner.nextLine();

        VisitorEntry entry = new VisitorEntry(visitor, resident, LocalDateTime.now());
        this.log.addEntry(entry);

        System.out.println("Visitor successfully signed in.");
    }

    // EFFECTS: displays all visitor entries
    public void viewAllVisitors() {
        List<VisitorEntry> entries = this.log.getAllEntries();
        if (entries.isEmpty()) {
            System.out.println("No visitor entries found.");
            return;
        }

        System.out.println("All Visitor Entries:");
        for (VisitorEntry e : entries) {
            String status = e.isSignedIn() ? "Signed in" : "Signed out";
            System.out.println("- " + e.getVisitorName() + " visiting " + e.getResidentName() + " (" + status + ")");
        }
    }

    // EFFECTS: displays all currently active visitors
    public void viewActiveVisitors() {
        List<VisitorEntry> active = this.log.getActiveVisitors();
        if (active.isEmpty()) {
            System.out.println("No active visitors at the moment.");
            return;
        }

        System.out.println("Active Visitors:");
        for (VisitorEntry e : active) {
            System.out.println("- " + e.getVisitorName() + " visiting " + e.getResidentName());
        }
    }

    // MODIFIES: this
    // EFFECTS: prompts the user to enter a visitor name and signs them out if found
    public void signOutVisitor() {
        System.out.println("Enter the name of the visitor to sign out:");
        String name = this.scanner.nextLine();

        for (VisitorEntry e : log.getAllEntries()) {
            if (e.getVisitorName().equals(name) && e.isSignedIn()) {
                e.signOut(LocalDateTime.now());
                System.out.println(name + " has been signed out.");
                System.out.println("Time signed in: " + e.getTimeIn());
                System.out.println("Time signed out: " + e.getTimeOut());
                System.out.println("Total visit duration: " + e.getDurationMinutes() + " minutes.");
                return;
            }
        }
        System.out.println("No signed-in visitor found with that name.");
    }

    // EFFECTS: shows the most frequent visitor
    public void showMostFrequentVisitor() {
        String frequent = this.log.getMostFrequentVisitor();
        if (frequent == null) {
            System.out.println("No visitor records found.");
        } else {
            System.out.println("Most frequent visitor: " + frequent);
        }
    }

    // EFFECTS: shows the most frequently visited resident
    public void showMostVisitedResident() {
        String resident = this.log.getMostVisitedResident();
        if (resident == null) {
            System.out.println("No visitor records found.");
        } else {
            System.out.println("Most frequently visited resident: " + resident);
        }
    }

    // EFFECTS: shows the average visit duration
    public void showAverageVisitDuration() {
        double avg = this.log.getAverageVisitLength();
        System.out.println("Average visit duration: " + avg + " minutes");
    }

    // EFFECTS: saves the visitor log to file
    public void saveVisitorLog() {
        try {
            jsonWriter.open();
            jsonWriter.write(log);
            jsonWriter.close();
            System.out.println("Saved visitor log to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: loads the visitor log from file
    public void loadVisitorLog() {
        try {
            log = jsonReader.read();
            System.out.println("Loaded visitor log from " + JSON_STORE);
        } catch (IOException e) {
            System.out.println("Unable to read from file: " + JSON_STORE);
        }
    }

    // MODIFIES: this
    // EFFECTS: quits the application and prints a farewell message
    public void quitApplication() {
        System.out.println("Thank you for using the Visitor Log App!");
        this.isProgramRunning = false;
    }

    // EFFECTS: prints a line divider
    public void printDivider() {
        System.out.println("------------------------------------");
    }
}
