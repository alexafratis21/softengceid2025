import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ConcertHistoryDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public ConcertHistoryDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ConcertHistory Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert ConcertHistory");
        JButton updateButton = new JButton("Update ConcertHistory");
        JButton deleteButton = new JButton("Delete ConcertHistory");
        JButton searchButton = new JButton("Search By Criteria");

        insertButton.addActionListener(e -> insertConcertHistoryData());
        updateButton.addActionListener(e -> updateConcertHistoryData());
        deleteButton.addActionListener(e -> deleteConcertHistoryData());
        searchButton.addActionListener(e -> showSearchDialog());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(searchButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadConcertHistoryData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadConcertHistoryData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM concert_history");
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
            JOptionPane.showMessageDialog(this, "Error loading concert history data: " + ex.getMessage());
        }
    }

    private void insertConcertHistoryData() {
        try {
            String concertQuery = "SELECT ConcertID, ArtistID, VenueID, ConcertDate, Tickets FROM concert";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(concertQuery);

            Map<Integer, String> concerts = new HashMap<>();
            while (rs.next()) {
                concerts.put(rs.getInt("ConcertID"), rs.getString("ConcertDate"));
            }

            JComboBox<Integer> concertComboBox = new JComboBox<>(concerts.keySet().toArray(new Integer[0]));
            JTextField ticketsField = new JTextField();
            JTextField concertDateField = new JTextField();
            JComboBox<String> statusComboBox = new JComboBox<>(new String[] { "COMPLETED" });
            JTextField artistNameField = new JTextField();


            String[] venues = {
                "Athens Concert Arena", "Thessaloniki Dome", "Patras Open Air", 
                "Heraklion Stage", "Ioannina Amphitheater", "Larisa Music Garden", 
                "Volos City Hall", "Rhodes Summer Arena", "Corfu Opera House", 
                "Olympia Music Hall"
            };
            JComboBox<String> venueComboBox = new JComboBox<>(venues);

            JPanel panel = new JPanel(new GridLayout(7, 2));
            panel.add(new JLabel("Select Concert:"));
            panel.add(concertComboBox);
            panel.add(new JLabel("Artist Name:")); 
            panel.add(artistNameField);
            panel.add(new JLabel("Select Venue:"));
            panel.add(venueComboBox); 
            panel.add(new JLabel("Tickets:"));
            panel.add(ticketsField);
            panel.add(new JLabel("Concert Date (YYYY-MM-DD):"));
            panel.add(concertDateField);
            panel.add(new JLabel("Status:"));
            panel.add(statusComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Concert History", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer concertID = (Integer) concertComboBox.getSelectedItem();
                String tickets = ticketsField.getText();
                String concertDate = concertDateField.getText();
                String status = (String) statusComboBox.getSelectedItem();
                String artistName = artistNameField.getText();
                String venueName = (String) venueComboBox.getSelectedItem(); 

                if (artistName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Artist Name cannot be empty.");
                    return;
                }

                String insertSQL = "INSERT INTO concert_history (ConcertID, ArtistName, VenueName, Tickets, ConcertDate, Status) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPstmt = conn.prepareStatement(insertSQL);
                insertPstmt.setInt(1, concertID);
                insertPstmt.setString(2, artistName);
                insertPstmt.setString(3, venueName); 
                insertPstmt.setInt(4, Integer.parseInt(tickets));
                insertPstmt.setString(5, concertDate);
                insertPstmt.setString(6, status);
                insertPstmt.executeUpdate();

                loadConcertHistoryData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting concert history data: " + ex.getMessage());
        }
    }

    private void updateConcertHistoryData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int concertHistoryID = (int) dataTable.getValueAt(selectedRow, 0);
            int concertID = (int) dataTable.getValueAt(selectedRow, 1);  

            Object ticketsObj = dataTable.getValueAt(selectedRow, 4);
            Object concertDateObj = dataTable.getValueAt(selectedRow, 5);
            Object venueObj = dataTable.getValueAt(selectedRow, 3); 

            String tickets = ticketsObj != null ? ticketsObj.toString() : "";
            String concertDate = concertDateObj != null ? concertDateObj.toString() : "";

            JTextField ticketsField = new JTextField(tickets);
            JTextField concertDateField = new JTextField(concertDate);


            String[] venues = {
                "Athens Concert Arena", "Thessaloniki Dome", "Patras Open Air", 
                "Heraklion Stage", "Ioannina Amphitheater", "Larisa Music Garden", 
                "Volos City Hall", "Rhodes Summer Arena", "Corfu Opera House", 
                "Olympia Music Hall"
            };
            JComboBox<String> venueComboBox = new JComboBox<>(venues);
            venueComboBox.setSelectedItem(venueObj);

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("Tickets:"));
            panel.add(ticketsField);
            panel.add(new JLabel("Concert Date (YYYY-MM-DD):"));
            panel.add(concertDateField);
            panel.add(new JLabel("Select Venue:"));
            panel.add(venueComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Update Concert History", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String newTickets = ticketsField.getText();
                String newConcertDate = concertDateField.getText();
                String newVenueName = (String) venueComboBox.getSelectedItem();

                String updateSQL = "UPDATE concert_history SET Tickets = ?, ConcertDate = ?, VenueName = ? WHERE ConcertHistoryID = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                pstmt.setInt(1, Integer.parseInt(newTickets));
                pstmt.setString(2, newConcertDate);
                pstmt.setString(3, newVenueName); 
                pstmt.setInt(4, concertHistoryID);
                pstmt.executeUpdate();

                loadConcertHistoryData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating concert history data: " + ex.getMessage());
        }
    }

    private void deleteConcertHistoryData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int concertHistoryID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM concert_history WHERE ConcertHistoryID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, concertHistoryID);
            pstmt.executeUpdate();

            loadConcertHistoryData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting concert history data: " + ex.getMessage());
        }
    }

    private void showSearchDialog() {
        JDialog searchDialog = new JDialog((Frame) null, "Search By Criteria", true);
        searchDialog.setLayout(new FlowLayout());
        
        JButton searchArtistsByTicketsButton = new JButton("Search Artists By Tickets");
        JButton searchConcertDatesByVenueButton = new JButton("Search Concert Dates By Venue");

        searchArtistsByTicketsButton.addActionListener(e -> showSearchArtistsByTickets());
        searchConcertDatesByVenueButton.addActionListener(e -> showSearchConcertDatesByVenue());

        searchDialog.add(searchArtistsByTicketsButton);
        searchDialog.add(searchConcertDatesByVenueButton);
        searchDialog.setSize(300, 100);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
    }

    private void showSearchArtistsByTickets() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Min Tickets:"));
        JTextField minTicketsField = new JTextField();
        panel.add(minTicketsField);
        panel.add(new JLabel("Max Tickets:"));
        JTextField maxTicketsField = new JTextField();
        panel.add(maxTicketsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Search Artists By Tickets", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int minTickets = Integer.parseInt(minTicketsField.getText());
            int maxTickets = Integer.parseInt(maxTicketsField.getText());
            searchArtistsByTickets(minTickets, maxTickets);
        }
    }

    private void searchArtistsByTickets(int minTickets, int maxTickets) {
        try {
            CallableStatement stmt = conn.prepareCall("{CALL SearchArtistsByTickets(?, ?)}");
            stmt.setInt(1, minTickets);
            stmt.setInt(2, maxTickets);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No artists found in this ticket range.");
                return;
            }

            JDialog resultsDialog = new JDialog((Frame) null, "Search Results", true);
            DefaultTableModel resultsTableModel = new DefaultTableModel();
            JTable resultsTable = new JTable(resultsTableModel);
            JScrollPane scrollPane = new JScrollPane(resultsTable);
            resultsDialog.add(scrollPane, BorderLayout.CENTER);
            resultsDialog.setSize(400, 300);
            resultsDialog.setLocationRelativeTo(this);

            resultsTableModel.addColumn("Artist Name");
            while (rs.next()) {
                String artistName = rs.getString("ArtistName");
                resultsTableModel.addRow(new Object[]{artistName});
            }
            resultsDialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching for artists by tickets: " + ex.getMessage());
        }
    }

    private void showSearchConcertDatesByVenue() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        String[] venues = {
            "Athens Concert Arena", "Thessaloniki Dome", "Patras Open Air", 
            "Heraklion Stage", "Ioannina Amphitheater", "Larisa Music Garden", 
            "Volos City Hall", "Rhodes Summer Arena", "Corfu Opera House", 
            "Olympia Music Hall"
        };
        JComboBox<String> venueComboBox = new JComboBox<>(venues);
        panel.add(new JLabel("Select Venue:"));
        panel.add(venueComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Search Concert Dates By Venue", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String venueName = (String) venueComboBox.getSelectedItem();
            searchConcertDatesByVenue(venueName);
        }
    }

    private void searchConcertDatesByVenue(String venueName) {
        try {
            CallableStatement stmt = conn.prepareCall("{CALL SearchConcertDatesByVenue(?)}");
            stmt.setString(1, venueName);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No concert dates found for the selected venue.");
                return;
            }

            JDialog resultsDialog = new JDialog((Frame) null, "Search Results", true);
            DefaultTableModel resultsTableModel = new DefaultTableModel();
            JTable resultsTable = new JTable(resultsTableModel);
            JScrollPane scrollPane = new JScrollPane(resultsTable);
            resultsDialog.add(scrollPane, BorderLayout.CENTER);
            resultsDialog.setSize(400, 300);
            resultsDialog.setLocationRelativeTo(this);

            resultsTableModel.addColumn("Concert Date");
            while (rs.next()) {
                java.sql.Date concertDate = rs.getDate("ConcertDate");
                resultsTableModel.addRow(new Object[]{concertDate});
            }
            resultsDialog.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching concert dates by venue: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConcertHistoryDatabaseGUI().setVisible(true));
    }
}
