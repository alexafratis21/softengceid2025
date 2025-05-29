import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class BandDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public BandDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Band Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Band");
        JButton updateButton = new JButton("Update Band");
        JButton deleteButton = new JButton("Delete Band");

        insertButton.addActionListener(e -> insertBandData());
        updateButton.addActionListener(e -> updateBandData());
        deleteButton.addActionListener(e -> deleteBandData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadBandData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadBandData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM band");
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
            JOptionPane.showMessageDialog(this, "Error loading band data: " + ex.getMessage());
        }
    }

    private void insertBandData() {
        try {
            String sql = "SELECT ArtistID, ArtistType FROM artist";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            Map<Integer, String> artists = new HashMap<>();
            while (rs.next()) {
                artists.put(rs.getInt("ArtistID"), rs.getString("ArtistType"));
            }

            JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));
            JTextField bandNameField = new JTextField();
            JTextField formationDateField = new JTextField();
            JTextField disbandDateField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("Select Artist:"));
            panel.add(artistComboBox);
            panel.add(new JLabel("Band Name:"));
            panel.add(bandNameField);
            panel.add(new JLabel("Formation Date (YYYY-MM-DD):"));
            panel.add(formationDateField);
            panel.add(new JLabel("Disband Date (YYYY-MM-DD or leave blank):"));
            panel.add(disbandDateField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Band", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer artistID = (Integer) artistComboBox.getSelectedItem();
                String bandName = bandNameField.getText();
                String formationDate = formationDateField.getText();
                String disbandDate = disbandDateField.getText().isEmpty() ? null : disbandDateField.getText();

                String insertSQL = "INSERT INTO band (BandName, FormationDate, DisbandDate, ArtistID) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, bandName);
                pstmt.setString(2, formationDate);
                pstmt.setObject(3, disbandDate);
                pstmt.setInt(4, artistID);
                pstmt.executeUpdate();

                loadBandData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting band data: " + ex.getMessage());
        }
    }

    private void updateBandData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int bandID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT BandName, FormationDate, DisbandDate FROM band WHERE BandID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bandID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String bandName = rs.getString("BandName");
                String formationDate = rs.getString("FormationDate");
                String disbandDate = rs.getString("DisbandDate");

                JTextField bandNameField = new JTextField(bandName);
                JTextField formationDateField = new JTextField(formationDate);
                JTextField disbandDateField = new JTextField(disbandDate);

                JPanel panel = new JPanel(new GridLayout(3, 2));
                panel.add(new JLabel("Band Name:"));
                panel.add(bandNameField);
                panel.add(new JLabel("Formation Date:"));
                panel.add(formationDateField);
                panel.add(new JLabel("Disband Date (leave blank if still active):"));
                panel.add(disbandDateField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Band", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE band SET BandName = ?, FormationDate = ?, DisbandDate = ? WHERE BandID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, bandNameField.getText());
                    pstmt.setString(2, formationDateField.getText());
                    pstmt.setObject(3, disbandDateField.getText().isEmpty() ? null : disbandDateField.getText());
                    pstmt.setInt(4, bandID);
                    pstmt.executeUpdate();

                    loadBandData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating band data: " + ex.getMessage());
        }
    }

    private void deleteBandData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int bandID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM band WHERE BandID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, bandID);
            pstmt.executeUpdate();

            loadBandData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting band data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BandDatabaseGUI().setVisible(true));
    }
}
