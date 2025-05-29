import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class VenueDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTable scoreTable;  
    private DefaultTableModel scoreTableModel; 

    public VenueDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Venue Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        scoreTableModel = new DefaultTableModel();
        scoreTable = new JTable(scoreTableModel);
        JPanel scorePanel = new JPanel(new BorderLayout());
        scorePanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Venue");
        JButton updateButton = new JButton("Update Venue");
        JButton deleteButton = new JButton("Delete Venue");
        JButton venueScoresButton = new JButton("Venue Scores");  

        insertButton.addActionListener(e -> insertVenueData());
        updateButton.addActionListener(e -> updateVenueData());
        deleteButton.addActionListener(e -> deleteVenueData());
        venueScoresButton.addActionListener(e -> showVenueScores()); 

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(venueScoresButton);  

        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadVenueData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadVenueData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM venue");
            ResultSetMetaData meta = rs.getMetaData();

            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(meta.getColumnName(i));
            }

            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading venue data: " + ex.getMessage());
        }
    }

    private void showVenueScores() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VenueID, VenueName FROM venue");

            scoreTableModel.setRowCount(0);
            scoreTableModel.setColumnCount(0);
            scoreTableModel.addColumn("VenueID");
            scoreTableModel.addColumn("VenueName");
            scoreTableModel.addColumn("VenueScore");

            while (rs.next()) {
                int venueID = rs.getInt("VenueID");
                String venueName = rs.getString("VenueName");

                CallableStatement stmtScore = conn.prepareCall("{call CalculateVenueScore(?, ?)}");
                stmtScore.setInt(1, venueID);
                stmtScore.registerOutParameter(2, Types.DECIMAL);
                stmtScore.execute();

                BigDecimal venueScore = stmtScore.getBigDecimal(2);

                scoreTableModel.addRow(new Object[]{venueID, venueName, venueScore});
            }

            JOptionPane.showMessageDialog(this, new JScrollPane(scoreTable), "Venue Scores", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving venue scores: " + ex.getMessage());
        }
    }

    private void insertVenueData() {
        try {
            JTextField venueNameField = new JTextField();
            JTextField capacityField = new JTextField();
            JTextField openYearField = new JTextField();
            JTextField completedConcertsField = new JTextField("0");

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Venue Name:"));
            panel.add(venueNameField);
            panel.add(new JLabel("Capacity:"));
            panel.add(capacityField);
            panel.add(new JLabel("Open Year:"));
            panel.add(openYearField);
            panel.add(new JLabel("Completed Concerts:"));
            panel.add(completedConcertsField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Venue", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String venueName = venueNameField.getText();
                int capacity = Integer.parseInt(capacityField.getText());
                int openYear = Integer.parseInt(openYearField.getText());
                int completedConcerts = Integer.parseInt(completedConcertsField.getText());

                String insertSQL = "INSERT INTO venue (VenueName, Capacity, OpenYear, CompletedConcerts) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, venueName);
                pstmt.setInt(2, capacity);
                pstmt.setInt(3, openYear);
                pstmt.setInt(4, completedConcerts);
                pstmt.executeUpdate();

                loadVenueData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting venue data: " + ex.getMessage());
        }
    }

    private void updateVenueData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int venueID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT VenueName, Capacity, OpenYear, CompletedConcerts FROM venue WHERE VenueID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, venueID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String venueName = rs.getString("VenueName");
                int capacity = rs.getInt("Capacity");
                int openYear = rs.getInt("OpenYear");
                int completedConcerts = rs.getInt("CompletedConcerts");

                JTextField venueNameField = new JTextField(venueName);
                JTextField capacityField = new JTextField(String.valueOf(capacity));
                JTextField openYearField = new JTextField(String.valueOf(openYear));
                JTextField completedConcertsField = new JTextField(String.valueOf(completedConcerts));

                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("Venue Name:"));
                panel.add(venueNameField);
                panel.add(new JLabel("Capacity:"));
                panel.add(capacityField);
                panel.add(new JLabel("Open Year:"));
                panel.add(openYearField);
                panel.add(new JLabel("Completed Concerts:"));
                panel.add(completedConcertsField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Venue", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE venue SET VenueName = ?, Capacity = ?, OpenYear = ?, CompletedConcerts = ? WHERE VenueID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, venueNameField.getText());
                    pstmt.setInt(2, Integer.parseInt(capacityField.getText()));
                    pstmt.setInt(3, Integer.parseInt(openYearField.getText()));
                    pstmt.setInt(4, Integer.parseInt(completedConcertsField.getText()));
                    pstmt.setInt(5, venueID);
                    pstmt.executeUpdate();

                    loadVenueData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating venue data: " + ex.getMessage());
        }
    }

    private void deleteVenueData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int venueID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM venue WHERE VenueID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, venueID);
            pstmt.executeUpdate();

            loadVenueData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting venue data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VenueDatabaseGUI().setVisible(true));
    }
}
