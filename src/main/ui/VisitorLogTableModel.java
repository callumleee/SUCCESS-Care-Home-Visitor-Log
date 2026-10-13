package ui;

import model.VisitorLog;
import model.VisitorEntry;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.List;

// A custom TableModel to display VisitorLog data in a JTable
public class VisitorLogTableModel extends AbstractTableModel {
    private VisitorLog log;
    private final List<String> columnNames;

    // EFFECTS: constructs the table model with a given visitor log
    public VisitorLogTableModel(VisitorLog log) {
        this.log = log;

        this.columnNames = new ArrayList<>();
        this.columnNames.add("Visitor");
        this.columnNames.add("Resident");
        this.columnNames.add("Time In");
        this.columnNames.add("Time Out");
    }

    @Override
    public int getRowCount() {
        return log.getTotalEntries();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        VisitorEntry entry = log.getAllEntries().get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.getVisitorName();
            case 1:
                return entry.getResidentName();
            case 2:
                return entry.getTimeIn().toString().replace('T', ' ');
            case 3:
                if (entry.getTimeOut() == null) {
                    return "Still signed in";
                } else {
                    return entry.getTimeOut().toString().replace('T', ' ');
                }
            default:
                return null;
        }
    }

    // MODIFIES: this
    // EFFECTS: refreshes the table data by causing a "data changed" event
    public void refresh() {
        fireTableDataChanged();
    }

    // MODIFIES: this
    // EFFECTS: sets the visitor log to a new one (when loading from the database)
    public void setLog(VisitorLog newLog) {
        this.log = newLog;
        refresh();
    }
}