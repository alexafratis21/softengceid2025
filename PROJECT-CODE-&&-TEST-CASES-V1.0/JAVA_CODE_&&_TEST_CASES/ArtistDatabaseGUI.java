
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;



public class ArtistDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public ArtistDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Artist Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Artist");
        JButton updateButton = new JButton("Update Artist");
        JButton deleteButton = new JButton("Delete Artist");

        insertButton.addActionListener(e -> insertArtistData());
        updateButton.addActionListener(e -> updateArtistData());
        deleteButton.addActionListener(e -> deleteArtistData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadArtistData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadArtistData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM artist");
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
            JOptionPane.showMessageDialog(this, "Error loading artist data: " + ex.getMessage());
        }
    }

    private void insertArtistData() {
        try {
            JComboBox<String> artistTypeComboBox = new JComboBox<>(new String[]{"PERSON", "BAND", "ORCHESTRA", "CHOIR", "VIRTUAL", "OTHER"});

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Select Artist Type:"));
            panel.add(artistTypeComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Artist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String artistType = (String) artistTypeComboBox.getSelectedItem();

                String insertSQL = "INSERT INTO artist (ArtistType) VALUES (?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, artistType);
                pstmt.executeUpdate();

                loadArtistData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting artist data: " + ex.getMessage());
        }
    }

    private void updateArtistData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int artistID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT ArtistType FROM artist WHERE ArtistID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, artistID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String artistType = rs.getString("ArtistType");

                JComboBox<String> artistTypeComboBox = new JComboBox<>(new String[]{"PERSON", "BAND", "ORCHESTRA", "CHOIR", "VIRTUAL", "OTHER"});
                artistTypeComboBox.setSelectedItem(artistType);

                JPanel panel = new JPanel(new GridLayout(2, 2));
                panel.add(new JLabel("Select Artist Type:"));
                panel.add(artistTypeComboBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Artist", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE artist SET ArtistType = ? WHERE ArtistID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, (String) artistTypeComboBox.getSelectedItem());
                    pstmt.setInt(2, artistID);
                    pstmt.executeUpdate();

                    loadArtistData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating artist data: " + ex.getMessage());
        }
    }

    private void deleteArtistData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int artistID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM artist WHERE ArtistID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, artistID);
            pstmt.executeUpdate();

            loadArtistData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting artist data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ArtistDatabaseGUI().setVisible(true));
    }
}



