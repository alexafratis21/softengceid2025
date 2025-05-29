
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class DBADatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public DBADatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("DBA Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert DBA");
        JButton updateButton = new JButton("Update DBA");
        JButton deleteButton = new JButton("Delete DBA");

        insertButton.addActionListener(e -> insertDBAData());
        updateButton.addActionListener(e -> updateDBAData());
        deleteButton.addActionListener(e -> deleteDBAData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadDBAData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadDBAData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM dba");
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
            JOptionPane.showMessageDialog(this, "Error loading DBA data: " + ex.getMessage());
        }
    }

    private void insertDBAData() {
        try {
            JTextField usernameField = new JTextField();
            JTextField startDateField = new JTextField();
            JTextField endDateField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(4, 2));
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
            panel.add(startDateField);
            panel.add(new JLabel("End Date (YYYY-MM-DD):"));
            panel.add(endDateField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert DBA", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String startDate = startDateField.getText();
                String endDate = endDateField.getText();

                String insertSQL = "INSERT INTO dba (Username, StartDate, EndDate) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, username);
                pstmt.setString(2, startDate);
                pstmt.setString(3, endDate.isEmpty() ? null : endDate);
                pstmt.executeUpdate();

                loadDBAData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting DBA data: " + ex.getMessage());
        }
    }

    private void updateDBAData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int dbaID = (int) dataTable.getValueAt(selectedRow, 0);
            String sql = "SELECT Username, StartDate, EndDate FROM dba WHERE DBAID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, dbaID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("Username");
                String startDate = rs.getString("StartDate");
                String endDate = rs.getString("EndDate");

                JTextField usernameField = new JTextField(username);
                JTextField startDateField = new JTextField(startDate);
                JTextField endDateField = new JTextField(endDate);

                JPanel panel = new JPanel(new GridLayout(4, 2));
                panel.add(new JLabel("Username:"));
                panel.add(usernameField);
                panel.add(new JLabel("Start Date:"));
                panel.add(startDateField);
                panel.add(new JLabel("End Date:"));
                panel.add(endDateField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update DBA", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE dba SET Username = ?, StartDate = ?, EndDate = ? WHERE DBAID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, usernameField.getText());
                    pstmt.setString(2, startDateField.getText());
                    pstmt.setString(3, endDateField.getText());
                    pstmt.setInt(4, dbaID);
                    pstmt.executeUpdate();

                    loadDBAData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating DBA data: " + ex.getMessage());
        }
    }

    private void deleteDBAData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int dbaID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM dba WHERE DBAID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, dbaID);
            pstmt.executeUpdate();

            loadDBAData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting DBA data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DBADatabaseGUI().setVisible(true));
    }
}

