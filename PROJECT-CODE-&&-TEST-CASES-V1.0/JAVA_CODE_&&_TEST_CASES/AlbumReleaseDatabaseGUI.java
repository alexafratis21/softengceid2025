
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class AlbumReleaseDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public AlbumReleaseDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("AlbumRelease Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert AlbumRelease");
        JButton updateButton = new JButton("Update AlbumRelease");
        JButton deleteButton = new JButton("Delete AlbumRelease");

        insertButton.addActionListener(e -> insertAlbumReleaseData());
        updateButton.addActionListener(e -> updateAlbumReleaseData());
        deleteButton.addActionListener(e -> deleteAlbumReleaseData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadAlbumReleaseData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadAlbumReleaseData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM albumrelease");
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
            JOptionPane.showMessageDialog(this, "Error loading album release data: " + ex.getMessage());
        }
    }

    private void insertAlbumReleaseData() {
        try {
            String albumQuery = "SELECT AlbumID, Title FROM album";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(albumQuery);
            Map<Integer, String> albums = new HashMap<>();
            while (rs.next()) {
                albums.put(rs.getInt("AlbumID"), rs.getString("Title"));
            }

            JComboBox<Integer> albumComboBox = new JComboBox<>(albums.keySet().toArray(new Integer[0]));

            JTextField releaseDateField = new JTextField();
            JComboBox<String> releaseTypeComboBox = new JComboBox<>(new String[]{"LP", "CD", "MP3"});
            JComboBox<String> releaseStatusComboBox = new JComboBox<>(new String[]{"OFFICIAL", "PROMOTION", "BOOTLEG", "WITHDRAWN", "CANCELED"});
            JComboBox<String> packagingComboBox = new JComboBox<>(new String[]{"BOOK", "CARDBOARD SLEEVE", "DIGIPAK", "JEWEL CASE", "NA"});

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Select Album:"));
            panel.add(albumComboBox);
            panel.add(new JLabel("Release Date (YYYY-MM-DD):"));
            panel.add(releaseDateField);
            panel.add(new JLabel("Release Type:"));
            panel.add(releaseTypeComboBox);
            panel.add(new JLabel("Release Status:"));
            panel.add(releaseStatusComboBox);
            panel.add(new JLabel("Packaging:"));
            panel.add(packagingComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Album Release", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer albumID = (Integer) albumComboBox.getSelectedItem();
                String releaseDate = releaseDateField.getText();
                String releaseType = (String) releaseTypeComboBox.getSelectedItem();
                String releaseStatus = (String) releaseStatusComboBox.getSelectedItem();
                String packaging = (String) packagingComboBox.getSelectedItem();

                String insertSQL = "INSERT INTO albumrelease (AlbumID, ReleaseDate, ReleaseType, ReleaseStatus, Packaging) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setInt(1, albumID);
                pstmt.setString(2, releaseDate);
                pstmt.setString(3, releaseType);
                pstmt.setString(4, releaseStatus);
                pstmt.setString(5, packaging);
                pstmt.executeUpdate();

                loadAlbumReleaseData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting album release data: " + ex.getMessage());
        }
    }

    private void updateAlbumReleaseData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int releaseID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT AlbumID, ReleaseDate, ReleaseType, ReleaseStatus, Packaging FROM albumrelease WHERE ReleaseID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, releaseID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int albumID = rs.getInt("AlbumID");
                String releaseDate = rs.getString("ReleaseDate");
                String releaseType = rs.getString("ReleaseType");
                String releaseStatus = rs.getString("ReleaseStatus");
                String packaging = rs.getString("Packaging");

                JComboBox<Integer> albumComboBox = new JComboBox<>(new Integer[] { albumID });
                JTextField releaseDateField = new JTextField(releaseDate);
                JComboBox<String> releaseTypeComboBox = new JComboBox<>(new String[]{"LP", "CD", "MP3"});
                JComboBox<String> releaseStatusComboBox = new JComboBox<>(new String[]{"OFFICIAL", "PROMOTION", "BOOTLEG", "WITHDRAWN", "CANCELED"});
                JComboBox<String> packagingComboBox = new JComboBox<>(new String[]{"BOOK", "CARDBOARD SLEEVE", "DIGIPAK", "JEWEL CASE", "NA"});

                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("Select Album:"));
                panel.add(albumComboBox);
                panel.add(new JLabel("Release Date:"));
                panel.add(releaseDateField);
                panel.add(new JLabel("Release Type:"));
                panel.add(releaseTypeComboBox);
                panel.add(new JLabel("Release Status:"));
                panel.add(releaseStatusComboBox);
                panel.add(new JLabel("Packaging:"));
                panel.add(packagingComboBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Album Release", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE albumrelease SET AlbumID = ?, ReleaseDate = ?, ReleaseType = ?, ReleaseStatus = ?, Packaging = ? WHERE ReleaseID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setInt(1, (Integer) albumComboBox.getSelectedItem());
                    pstmt.setString(2, releaseDateField.getText());
                    pstmt.setString(3, (String) releaseTypeComboBox.getSelectedItem());
                    pstmt.setString(4, (String) releaseStatusComboBox.getSelectedItem());
                    pstmt.setString(5, (String) packagingComboBox.getSelectedItem());
                    pstmt.setInt(6, releaseID);
                    pstmt.executeUpdate();

                    loadAlbumReleaseData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating album release data: " + ex.getMessage());
        }
    }

    private void deleteAlbumReleaseData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int releaseID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM albumrelease WHERE ReleaseID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, releaseID);
            pstmt.executeUpdate();

            loadAlbumReleaseData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting album release data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlbumReleaseDatabaseGUI().setVisible(true));
    }
}