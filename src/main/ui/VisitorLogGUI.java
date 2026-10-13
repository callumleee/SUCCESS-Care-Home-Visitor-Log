package ui;

import model.VisitorEntry;
import model.VisitorLog;
import persistence.SqliteVisitorRepository;
import persistence.VisitorRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.awt.event.ActionEvent;

import java.sql.SQLException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import model.Event;
import model.EventLog;


// GUI for the Visitor Log
public class VisitorLogGUI extends JFrame implements ActionListener {

    private static final String DB_PATH = "./data/visitorlog.db";
    private VisitorLog log;
    private VisitorRepository repository;
    private VisitorLogTableModel tableModel;
    private JTable table;

    // EFFECTS: constructs main GUI window
    public VisitorLogGUI() {
        super("Care Home Visitor Log");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(900, 600));

        log = new VisitorLog();

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

        loadFromDatabase();

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

        panel.add(signInButton);
        panel.add(signOutButton);

        return panel;

    }

    // EFFECTS: handles button click events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("signIn")) {
            handleSignIn();
        } else if (e.getActionCommand().equals("signOut")) {
            handleSignOut();
        }
    }

    // MODIFIES: this
    // EFFECTS: prompts user for visitor/resident and adds a new entry,
    // saving it to the database
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
        try {
            repository.insert(newEntry);
        } catch (SQLException e) {
            showDatabaseError(e);
            return;
        }
        log.addEntry(newEntry);
        tableModel.refresh();
    }

    // MODIFIES: this
    // EFFECTS: signs out the currently selected visitor and saves it to the database
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
            LocalDateTime now = LocalDateTime.now();
            try {
                repository.signOut(entry.getId(), now);
            } catch (SQLException e) {
                showDatabaseError(e);
                return;
            }
            entry.signOut(now);
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

    // MODIFIES: this
    // EFFECTS: opens the database and loads all saved visits into the table;
    // exits if the database cannot be opened
    private void loadFromDatabase() {
        try {
            repository = new SqliteVisitorRepository(DB_PATH);
            log = new VisitorLog(repository.findAll());
            tableModel.setLog(log);
        } catch (SQLException e) {
            showDatabaseError(e);
            System.exit(1);
        }
    }

    // EFFECTS: prints the event log and closes the application
    private void handleQuit() {
        printLog();
        System.exit(0);
    }

    // EFFECTS: shows an error dialog for a database problem
    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // EFFECTS: prints the events in the EventLog to the console
    private void printLog() {
        for (Event next : EventLog.getInstance()) {
            System.out.println(next.toString());
        }
    }
}