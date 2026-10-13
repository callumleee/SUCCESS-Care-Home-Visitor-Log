package ui;

import model.VisitorEntry;
import model.VisitorLog;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.awt.event.ActionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import model.Event;
import model.EventLog;


// GUI for the Visitor Log
public class VisitorLogGUI extends JFrame implements ActionListener {

    private static final String JSON_STORE = "./data/visitorlog.json";
    private VisitorLog log;
    private JsonWriter jsonWriter;
    private JsonReader jsonReader;
    private VisitorLogTableModel tableModel;
    private JTable table;

    // EFFECTS: constructs main GUI window
    public VisitorLogGUI() {
        super("Care Home Visitor Log");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        log = new VisitorLog();
        jsonWriter = new JsonWriter(JSON_STORE);
        jsonReader = new JsonReader(JSON_STORE);

        setLayout(new BorderLayout());

        add(setUpHeaderPanel(), BorderLayout.NORTH);
        setUpTable();
        add(setUpButtonPanel(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuit();
            }
        });

        handleLoadOnStart();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // MODIFIES: this
    // EFFECTS: initializes the table and adds it to the center of the layout
    private void setUpTable() {
        tableModel = new VisitorLogTableModel(log);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // EFFECTS: creates a panel with action buttons
    private JPanel setUpButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton signInButton = new JButton("Sign In");
        signInButton.setActionCommand("signIn");
        signInButton.addActionListener(this);

        JButton signOutButton = new JButton("Sign Out");
        signOutButton.setActionCommand("signOut");
        signOutButton.addActionListener(this);

        JButton saveButton = new JButton("Save");
        saveButton.setActionCommand("save");
        saveButton.addActionListener(this);

        panel.add(signInButton);
        panel.add(signOutButton);
        panel.add(saveButton);

        return panel;

    }

    // EFFECTS: handles button click events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("signIn")) {
            handleSignIn();
        } else if (e.getActionCommand().equals("signOut")) {
            handleSignOut();
        } else if (e.getActionCommand().equals("save")) {
            handleSave();
        }
    }

    // MODIFIES: this
    // EFFECTS: prompts user for visitor/resident and adds a new entry
    private void handleSignIn() {
        String visitorName = JOptionPane.showInputDialog(this, "Enter Visitor Name:");
        if (visitorName == null || visitorName.isEmpty()) {
            return;
        }

        String residentName = JOptionPane.showInputDialog(this, "Enter Resident Name:");
        if (residentName == null || residentName.isEmpty()) {
            return;
        }

        VisitorEntry newEntry = new VisitorEntry(visitorName, residentName, LocalDateTime.now());
        log.addEntry(newEntry);
        tableModel.refresh();
    }

    // MODIFIES: this
    // EFFECTS: signs out the currently selected visitor
    private void handleSignOut() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a visitor to sign out.");
            return;
        }

        VisitorEntry entry = log.getAllEntries().get(selectedRow);

        if (!entry.isSignedIn()) {
            JOptionPane.showMessageDialog(this, "This visitor is already signed out.");
        } else {
            entry.signOut(LocalDateTime.now());
            tableModel.refresh();
            JOptionPane.showMessageDialog(this, entry.getVisitorName() + " has been signed out.");
        }
    }

    // EFFECTS: creates a header panel with a logo and title
    private JPanel setUpHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.pink);

        JLabel title = new JLabel("Care Home Visitor Log");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        try {
            ImageIcon logo = new ImageIcon("./images/logo.png");
            Image img = logo.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(img));
            panel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        panel.add(title);
        return panel;
    }

    // EFFECTS: displays a load option on startup
    private void handleLoadOnStart() {
        int result = JOptionPane.showConfirmDialog(this, "Load previous visitor log?",
                "Load", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            try {
                log = jsonReader.read();
                tableModel.setLog(log);
                JOptionPane.showMessageDialog(this, "Loaded log from " + JSON_STORE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Unable to read from file: " + JSON_STORE);
            }
        }
    }

    // EFFECTS: displays a save option on exit
    private void handleQuit() {
        int result = JOptionPane.showConfirmDialog(this, "Save visitor log before quitting?",
                "Save", JOptionPane.YES_NO_CANCEL_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            handleSave();
            printLog();
            System.exit(0);
        } else if (result == JOptionPane.NO_OPTION) {
            printLog();
            System.exit(0);
        }
    }

    // EFFECTS: saves the visitor log to file
    private void handleSave() {
        try {
            jsonWriter.open();
            jsonWriter.write(log);
            jsonWriter.close();
            JOptionPane.showMessageDialog(this, "Saved visitor log to " + JSON_STORE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Unable to write to file: " + JSON_STORE);
        }
    }

    // EFFECTS: prints the events in the EventLog to the console
    private void printLog() {
        for (Event next : EventLog.getInstance()) {
            System.out.println(next.toString());
        }
    }
}