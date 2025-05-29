import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class BandMemberDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public BandMemberDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("BandMember Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert BandMember");
        JButton updateButton = new JButton("Update BandMember");
        JButton deleteButton = new JButton("Delete BandMember");

        insertButton.addActionListener(e -> insertBandMemberData());
        updateButton.addActionListener(e -> updateBandMemberData());
        deleteButton.addActionListener(e -> deleteBandMemberData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadBandMemberData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadBandMemberData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bandmember");
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
            JOptionPane.showMessageDialog(this, "Error loading bandmember data: " + ex.getMessage());
        }
    }

    private void insertBandMemberData() {
        try {
            String bandSql = "SELECT BandID, BandName FROM band";
            Statement stmt = conn.createStatement();
            ResultSet bandRs = stmt.executeQuery(bandSql);
            Map<Integer, String> bands = new HashMap<>();
            while (bandRs.next()) {
                bands.put(bandRs.getInt("BandID"), bandRs.getString("BandName"));
            }

            String personSql = "SELECT PersonID, FirstName, LastName FROM person";
            ResultSet personRs = stmt.executeQuery(personSql);
            Map<Integer, String> persons = new HashMap<>();
            while (personRs.next()) {
                persons.put(personRs.getInt("PersonID"), personRs.getString("FirstName") + " " + personRs.getString("LastName"));
            }

            JComboBox<Integer> bandComboBox = new JComboBox<>(bands.keySet().toArray(new Integer[0]));
            JComboBox<Integer> personComboBox = new JComboBox<>(persons.keySet().toArray(new Integer[0]));
            JTextField fromDateField = new JTextField();
            JTextField toDateField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Select Band:"));
            panel.add(bandComboBox);
            panel.add(new JLabel("Select Person:"));
            panel.add(personComboBox);
            panel.add(new JLabel("From Date (YYYY-MM-DD):"));
            panel.add(fromDateField);
            panel.add(new JLabel("To Date (YYYY-MM-DD or leave blank):"));
            panel.add(toDateField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert BandMember", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer bandID = (Integer) bandComboBox.getSelectedItem();
                Integer personID = (Integer) personComboBox.getSelectedItem();
                String fromDate = fromDateField.getText();
                String toDate = toDateField.getText().isEmpty() ? null : toDateField.getText();

                String insertSQL = "INSERT INTO bandmember (BandID, PersonID, FromDate, ToDate) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setInt(1, bandID);
                pstmt.setInt(2, personID);
                pstmt.setString(3, fromDate);
                pstmt.setObject(4, toDate);
                pstmt.executeUpdate();

                loadBandMemberData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting bandmember data: " + ex.getMessage());
        }
    }

    private void updateBandMemberData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int bandID = (int) dataTable.getValueAt(selectedRow, 0);
            int personID = (int) dataTable.getValueAt(selectedRow, 1);

            String sql = "SELECT FromDate, ToDate FROM bandmember WHERE BandID = ? AND PersonID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bandID);
            pstmt.setInt(2, personID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String fromDate = rs.getString("FromDate");
                String toDate = rs.getString("ToDate");

                JTextField fromDateField = new JTextField(fromDate);
                JTextField toDateField = new JTextField(toDate);

                JPanel panel = new JPanel(new GridLayout(3, 2));
                panel.add(new JLabel("From Date:"));
                panel.add(fromDateField);
                panel.add(new JLabel("To Date (leave blank if still active):"));
                panel.add(toDateField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update BandMember", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE bandmember SET FromDate = ?, ToDate = ? WHERE BandID = ? AND PersonID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, fromDateField.getText());
                    pstmt.setObject(2, toDateField.getText().isEmpty() ? null : toDateField.getText());
                    pstmt.setInt(3, bandID);
                    pstmt.setInt(4, personID);
                    pstmt.executeUpdate();

                    loadBandMemberData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating bandmember data: " + ex.getMessage());
        }
    }

    private void deleteBandMemberData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int bandID = (int) dataTable.getValueAt(selectedRow, 0);
            int personID = (int) dataTable.getValueAt(selectedRow, 1);

            String deleteSQL = "DELETE FROM bandmember WHERE BandID = ? AND PersonID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, bandID);
            pstmt.setInt(2, personID);
            pstmt.executeUpdate();

            loadBandMemberData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting bandmember data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BandMemberDatabaseGUI().setVisible(true));
    }
}
