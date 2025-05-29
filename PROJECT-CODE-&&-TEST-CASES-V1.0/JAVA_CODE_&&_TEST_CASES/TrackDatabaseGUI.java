
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class TrackDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public TrackDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Track Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Track");
        JButton updateButton = new JButton("Update Track");
        JButton deleteButton = new JButton("Delete Track");

        insertButton.addActionListener(e -> insertTrackData());
        updateButton.addActionListener(e -> updateTrackData());
        deleteButton.addActionListener(e -> deleteTrackData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadTrackData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadTrackData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM track");
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
            JOptionPane.showMessageDialog(this, "Error loading track data: " + ex.getMessage());
        }
    }

    private void insertTrackData() {
        try {
            String albumQuery = "SELECT AlbumID, Title FROM album";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(albumQuery);
            Map<Integer, String> albums = new HashMap<>();
            while (rs.next()) {
                albums.put(rs.getInt("AlbumID"), rs.getString("Title"));
            }

            JComboBox<Integer> albumComboBox = new JComboBox<>(albums.keySet().toArray(new Integer[0]));

            JTextField titleField = new JTextField();
            JTextField trackLengthField = new JTextField();
            JTextField trackNoField = new JTextField();
            JTextArea lyricsArea = new JTextArea();

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Select Album:"));
            panel.add(albumComboBox);
            panel.add(new JLabel("Track Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Track Length (HH:MM:SS):"));
            panel.add(trackLengthField);
            panel.add(new JLabel("Track Number:"));
            panel.add(trackNoField);
            panel.add(new JLabel("Lyrics:"));
            panel.add(new JScrollPane(lyricsArea));

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Track", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer albumID = (Integer) albumComboBox.getSelectedItem();
                String title = titleField.getText();
                String trackLength = trackLengthField.getText();
                int trackNo = Integer.parseInt(trackNoField.getText());
                String lyrics = lyricsArea.getText();

                String insertSQL = "INSERT INTO track (Title, AlbumID, TrackLength, TrackNo, Lyrics) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, title);
                pstmt.setInt(2, albumID);
                pstmt.setString(3, trackLength);
                pstmt.setInt(4, trackNo);
                pstmt.setString(5, lyrics);
                pstmt.executeUpdate();

                loadTrackData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting track data: " + ex.getMessage());
        }
    }

    private void updateTrackData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int trackID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT Title, AlbumID, TrackLength, TrackNo, Lyrics FROM track WHERE TrackID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trackID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String title = rs.getString("Title");
                int albumID = rs.getInt("AlbumID");
                String trackLength = rs.getString("TrackLength");
                int trackNo = rs.getInt("TrackNo");
                String lyrics = rs.getString("Lyrics");

                JComboBox<Integer> albumComboBox = new JComboBox<>(new Integer[] { albumID });
                JTextField titleField = new JTextField(title);
                JTextField trackLengthField = new JTextField(trackLength);
                JTextField trackNoField = new JTextField(String.valueOf(trackNo));
                JTextArea lyricsArea = new JTextArea(lyrics);

                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("Select Album:"));
                panel.add(albumComboBox);
                panel.add(new JLabel("Track Title:"));
                panel.add(titleField);
                panel.add(new JLabel("Track Length:"));
                panel.add(trackLengthField);
                panel.add(new JLabel("Track Number:"));
                panel.add(trackNoField);
                panel.add(new JLabel("Lyrics:"));
                panel.add(new JScrollPane(lyricsArea));

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Track", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE track SET Title = ?, AlbumID = ?, TrackLength = ?, TrackNo = ?, Lyrics = ? WHERE TrackID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, titleField.getText());
                    pstmt.setInt(2, (Integer) albumComboBox.getSelectedItem());
                    pstmt.setString(3, trackLengthField.getText());
                    pstmt.setInt(4, Integer.parseInt(trackNoField.getText()));
                    pstmt.setString(5, lyricsArea.getText());
                    pstmt.setInt(6, trackID);
                    pstmt.executeUpdate();

                    loadTrackData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating track data: " + ex.getMessage());
        }
    }

    private void deleteTrackData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int trackID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM track WHERE TrackID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, trackID);
            pstmt.executeUpdate();

            loadTrackData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting track data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrackDatabaseGUI().setVisible(true));
    }
}

