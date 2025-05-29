import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class RecordCompanyDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public RecordCompanyDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("RecordCompany Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert RecordCompany");
        JButton updateButton = new JButton("Update RecordCompany");
        JButton deleteButton = new JButton("Delete RecordCompany");

        insertButton.addActionListener(e -> insertRecordCompanyData());
        updateButton.addActionListener(e -> updateRecordCompanyData());
        deleteButton.addActionListener(e -> deleteRecordCompanyData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadRecordCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadRecordCompanyData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM recordcompany");
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
            JOptionPane.showMessageDialog(this, "Error loading recordcompany data: " + ex.getMessage());
        }
    }

    private void insertRecordCompanyData() {
        JTextField companyNameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField beginDateField = new JTextField();
        JTextField endDateField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(7, 2));
        panel.add(new JLabel("Company Name:"));
        panel.add(companyNameField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Begin Date (YYYY-MM-DD):"));
        panel.add(beginDateField);
        panel.add(new JLabel("End Date (YYYY-MM-DD or leave blank):"));
        panel.add(endDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert RecordCompany", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String companyName = companyNameField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String beginDate = beginDateField.getText();
            String endDate = endDateField.getText().isEmpty() ? null : endDateField.getText();

            String insertSQL = "INSERT INTO recordcompany (CompanyName, Address, Phone, Email, BeginDate, EndDate) VALUES (?, ?, ?, ?, ?, ?)";
            try {
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, companyName);
                pstmt.setString(2, address);
                pstmt.setString(3, phone);
                pstmt.setString(4, email);
                pstmt.setString(5, beginDate);
                pstmt.setObject(6, endDate); 
                pstmt.executeUpdate();

                loadRecordCompanyData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting recordcompany data: " + ex.getMessage());
            }
        }
    }

    private void updateRecordCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int companyID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT CompanyName, Address, Phone, Email, BeginDate, EndDate FROM recordcompany WHERE CompanyID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, companyID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String companyName = rs.getString("CompanyName");
                String address = rs.getString("Address");
                String phone = rs.getString("Phone");
                String email = rs.getString("Email");
                String beginDate = rs.getString("BeginDate");
                String endDate = rs.getString("EndDate");

                JTextField companyNameField = new JTextField(companyName);
                JTextField addressField = new JTextField(address);
                JTextField phoneField = new JTextField(phone);
                JTextField emailField = new JTextField(email);
                JTextField beginDateField = new JTextField(beginDate);
                JTextField endDateField = new JTextField(endDate);

                JPanel panel = new JPanel(new GridLayout(7, 2));
                panel.add(new JLabel("Company Name:"));
                panel.add(companyNameField);
                panel.add(new JLabel("Address:"));
                panel.add(addressField);
                panel.add(new JLabel("Phone:"));
                panel.add(phoneField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);
                panel.add(new JLabel("Begin Date:"));
                panel.add(beginDateField);
                panel.add(new JLabel("End Date (leave blank if no end date):"));
                panel.add(endDateField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update RecordCompany", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE recordcompany SET CompanyName = ?, Address = ?, Phone = ?, Email = ?, BeginDate = ?, EndDate = ? WHERE CompanyID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, companyNameField.getText());
                    pstmt.setString(2, addressField.getText());
                    pstmt.setString(3, phoneField.getText());
                    pstmt.setString(4, emailField.getText());
                    pstmt.setString(5, beginDateField.getText());
                    pstmt.setObject(6, endDateField.getText().isEmpty() ? null : endDateField.getText());
                    pstmt.setInt(7, companyID);
                    pstmt.executeUpdate();

                    loadRecordCompanyData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating recordcompany data: " + ex.getMessage());
        }
    }

    private void deleteRecordCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int companyID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM recordcompany WHERE CompanyID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, companyID);
            pstmt.executeUpdate();

            loadRecordCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting recordcompany data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecordCompanyDatabaseGUI().setVisible(true));
    }
}
