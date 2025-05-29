import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ConcertDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    private DefaultTableModel scoreTableModel;
    private JTable scoreTable;

    public ConcertDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Concert Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        scoreTableModel = new DefaultTableModel();
        scoreTable = new JTable(scoreTableModel);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Concert");
        JButton updateButton = new JButton("Update Concert");
        JButton deleteButton = new JButton("Delete Concert");

        insertButton.addActionListener(e -> insertConcertData());
        updateButton.addActionListener(e -> updateConcertData());
        deleteButton.addActionListener(e -> deleteConcertData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadConcertData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadConcertData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM concert");
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
            JOptionPane.showMessageDialog(this, "Error loading concert data: " + ex.getMessage());
        }
    }

    private void insertConcertData() {
        try {
            String artistQuery = "SELECT ArtistID, ArtistType FROM artist";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(artistQuery);

            Map<Integer, String> artists = new HashMap<>();
            while (rs.next()) {
                artists.put(rs.getInt("ArtistID"), rs.getString("ArtistType"));
            }

            JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));

            String venueQuery = "SELECT VenueID, VenueName FROM venue";
            ResultSet venueRs = stmt.executeQuery(venueQuery);
            Map<Integer, String> venues = new HashMap<>();
            while (venueRs.next()) {
                venues.put(venueRs.getInt("VenueID"), venueRs.getString("VenueName"));
            }

            JComboBox<Integer> venueComboBox = new JComboBox<>(venues.keySet().toArray(new Integer[0]));

            JTextField concertDateField = new JTextField();
            JTextField ticketsField = new JTextField("0");
            JComboBox<String> statusComboBox = new JComboBox<>(new String[] { "PLANNED", "COMPLETED", "CANCELED" });

            JLabel venueScoreLabel = new JLabel("Score: N/A");

            venueComboBox.addActionListener(e -> {
                Integer selectedVenueID = (Integer) venueComboBox.getSelectedItem();
                if (selectedVenueID != null) {
                    calculateVenueScore(selectedVenueID, venueScoreLabel);
                }
            });

            calculateVenueScore((Integer) venueComboBox.getSelectedItem(), venueScoreLabel);

            JButton showScoresButton = new JButton("Show All Scores");
            showScoresButton.addActionListener(e -> showVenueScores());

            JPanel panel = new JPanel(new GridLayout(8, 2));
            panel.add(new JLabel("Select Artist:"));
            panel.add(artistComboBox);
            panel.add(new JLabel("Select Venue:"));
            panel.add(venueComboBox);
            panel.add(new JLabel("Current Venue Score:"));
            panel.add(venueScoreLabel);
            panel.add(new JLabel("All Venue Scores:"));
            panel.add(showScoresButton); 
            panel.add(new JLabel("Concert Date (YYYY-MM-DD):"));
            panel.add(concertDateField);
            panel.add(new JLabel("Tickets:"));
            panel.add(ticketsField);
            panel.add(new JLabel("Status:"));
            panel.add(statusComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Concert", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer artistID = (Integer) artistComboBox.getSelectedItem();
                Integer venueID = (Integer) venueComboBox.getSelectedItem();
                String concertDate = concertDateField.getText();
                int tickets = Integer.parseInt(ticketsField.getText());
                String status = (String) statusComboBox.getSelectedItem();

                String insertSQL = "INSERT INTO concert (ArtistID, VenueID, ConcertDate, Tickets, Status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setInt(1, artistID);
                pstmt.setInt(2, venueID);
                pstmt.setString(3, concertDate);
                pstmt.setInt(4, tickets);
                pstmt.setString(5, status);
                pstmt.executeUpdate();

                loadConcertData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting concert data: " + ex.getMessage());
        }
    }

    private void updateConcertData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int concertID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT ArtistID, VenueID, ConcertDate, Tickets, Status FROM concert WHERE ConcertID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, concertID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int artistID = rs.getInt("ArtistID");
                int venueID = rs.getInt("VenueID");
                String concertDate = rs.getString("ConcertDate");
                int tickets = rs.getInt("Tickets");
                String status = rs.getString("Status");

                JTextField concertDateField = new JTextField(concertDate);
                JTextField ticketsField = new JTextField(String.valueOf(tickets));
                JComboBox<String> statusComboBox = new JComboBox<>(new String[] { "PLANNED", "COMPLETED", "CANCELED" });
                statusComboBox.setSelectedItem(status);

                String artistQuery = "SELECT ArtistID, ArtistType FROM artist";
                ResultSet artistRs = pstmt.executeQuery(artistQuery);
                Map<Integer, String> artists = new HashMap<>();
                while (artistRs.next()) {
                    artists.put(artistRs.getInt("ArtistID"), artistRs.getString("ArtistType"));
                }
                JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));
                artistComboBox.setSelectedItem(artistID);

                String venueQuery = "SELECT VenueID, VenueName FROM venue";
                ResultSet venueRs = pstmt.executeQuery(venueQuery);
                Map<Integer, String> venues = new HashMap<>();
                while (venueRs.next()) {
                    venues.put(venueRs.getInt("VenueID"), venueRs.getString("VenueName"));
                }
                JComboBox<Integer> venueComboBox = new JComboBox<>(venues.keySet().toArray(new Integer[0]));
                venueComboBox.setSelectedItem(venueID);

                JLabel venueScoreLabel = new JLabel("Score: N/A");

                venueComboBox.addActionListener(e -> {
                    Integer selectedVenueID = (Integer) venueComboBox.getSelectedItem();
                    if (selectedVenueID != null) {
                        calculateVenueScore(selectedVenueID, venueScoreLabel);
                    }
                });

                calculateVenueScore(venueID, venueScoreLabel);

                JButton showScoresButton = new JButton("Show All Scores");
                showScoresButton.addActionListener(e -> showVenueScores());

                JPanel panel = new JPanel(new GridLayout(8, 2));
                panel.add(new JLabel("Select Artist:"));
                panel.add(artistComboBox);
                panel.add(new JLabel("Select Venue:"));
                panel.add(venueComboBox);
                panel.add(new JLabel("Current Venue Score:"));
                panel.add(venueScoreLabel);
                panel.add(new JLabel("All Venue Scores:"));
                panel.add(showScoresButton);
                panel.add(new JLabel("Concert Date (YYYY-MM-DD):"));
                panel.add(concertDateField);
                panel.add(new JLabel("Tickets:"));
                panel.add(ticketsField);
                panel.add(new JLabel("Status:"));
                panel.add(statusComboBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Concert", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE concert SET ArtistID = ?, VenueID = ?, ConcertDate = ?, Tickets = ?, Status = ? WHERE ConcertID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setInt(1, (Integer) artistComboBox.getSelectedItem());
                    pstmt.setInt(2, (Integer) venueComboBox.getSelectedItem());
                    pstmt.setString(3, concertDateField.getText());
                    pstmt.setInt(4, Integer.parseInt(ticketsField.getText()));
                    pstmt.setString(5, (String) statusComboBox.getSelectedItem());
                    pstmt.setInt(6, concertID);
                    pstmt.executeUpdate();

                    loadConcertData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating concert data: " + ex.getMessage());
        }
    }
    
private void deleteConcertData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int concertID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM concert WHERE ConcertID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, concertID);
            pstmt.executeUpdate();

            loadConcertData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting concert data: " + ex.getMessage());
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

    private void calculateVenueScore(int venueID, JLabel venueScoreLabel) {
        try {
            String query = "{CALL CalculateVenueScore(?, ?)}";
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, venueID);
            stmt.registerOutParameter(2, Types.DECIMAL);

            stmt.execute();
            BigDecimal score = stmt.getBigDecimal(2);

            if (score != null) {
                score = score.setScale(2, RoundingMode.HALF_UP);
                venueScoreLabel.setText("Score: " + score.toString());
            } else {
                venueScoreLabel.setText("Score: N/A");
            }
        } catch (SQLException ex) {
            venueScoreLabel.setText("Score: Error");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConcertDatabaseGUI().setVisible(true));
    }
}
