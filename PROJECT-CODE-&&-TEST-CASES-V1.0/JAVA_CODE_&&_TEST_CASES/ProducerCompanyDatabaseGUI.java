import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ProducerCompanyDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public ProducerCompanyDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ProducerCompany Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert ProducerCompany");
        JButton updateButton = new JButton("Update ProducerCompany");
        JButton deleteButton = new JButton("Delete ProducerCompany");

        insertButton.addActionListener(e -> insertProducerCompanyData());
        updateButton.addActionListener(e -> updateProducerCompanyData());
        deleteButton.addActionListener(e -> deleteProducerCompanyData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadProducerCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadProducerCompanyData() {
        String query = "SELECT * FROM producercompany";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

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
            JOptionPane.showMessageDialog(this, "Error loading producer-company data: " + ex.getMessage());
        }
    }

    private void insertProducerCompanyData() {
        String producerSQL = "SELECT ProducerID, FirstName, LastName FROM producer";
        String companySQL = "SELECT CompanyID, CompanyName FROM recordcompany";

        try (PreparedStatement producerStmt = conn.prepareStatement(producerSQL);
             PreparedStatement companyStmt = conn.prepareStatement(companySQL);
             ResultSet rsProducer = producerStmt.executeQuery();
             ResultSet rsCompany = companyStmt.executeQuery()) {

            Map<Integer, String> producers = new HashMap<>();
            Map<Integer, String> companies = new HashMap<>();

            while (rsProducer.next()) {
                producers.put(rsProducer.getInt("ProducerID"), rsProducer.getString("FirstName") + " " + rsProducer.getString("LastName"));
            }

            while (rsCompany.next()) {
                companies.put(rsCompany.getInt("CompanyID"), rsCompany.getString("CompanyName"));
            }

            JComboBox<Integer> producerComboBox = new JComboBox<>(producers.keySet().toArray(new Integer[0]));
            JComboBox<Integer> companyComboBox = new JComboBox<>(companies.keySet().toArray(new Integer[0]));
            JTextField fromDateField = new JTextField();
            JTextField toDateField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Select Producer:"));
            panel.add(producerComboBox);
            panel.add(new JLabel("Select Company:"));
            panel.add(companyComboBox);
            panel.add(new JLabel("From Date (YYYY-MM-DD):"));
            panel.add(fromDateField);
            panel.add(new JLabel("To Date (YYYY-MM-DD or leave blank):"));
            panel.add(toDateField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Producer-Company", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer producerID = (Integer) producerComboBox.getSelectedItem();
                Integer companyID = (Integer) companyComboBox.getSelectedItem();
                String fromDate = fromDateField.getText();
                String toDate = toDateField.getText().isEmpty() ? null : toDateField.getText(); 

                String insertSQL = "INSERT INTO producercompany (ProducerID, RecordCompanyID, FromDate, ToDate) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setInt(1, producerID);
                    pstmt.setInt(2, companyID);
                    pstmt.setString(3, fromDate);
                    pstmt.setObject(4, toDate); 
                    pstmt.executeUpdate();
                }

                loadProducerCompanyData();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting producer-company data: " + ex.getMessage());
        }
    }

    private void updateProducerCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        int producerID = (int) dataTable.getValueAt(selectedRow, 0);
        int companyID = (int) dataTable.getValueAt(selectedRow, 1);

        try {
            String sql = "SELECT FromDate, ToDate FROM producercompany WHERE ProducerID = ? AND RecordCompanyID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, producerID);
                pstmt.setInt(2, companyID);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String fromDate = rs.getString("FromDate");
                        String toDate = rs.getString("ToDate");

                        JTextField fromDateField = new JTextField(fromDate);
                        JTextField toDateField = new JTextField(toDate);

                        JPanel panel = new JPanel(new GridLayout(3, 2));
                        panel.add(new JLabel("From Date:"));
                        panel.add(fromDateField);
                        panel.add(new JLabel("To Date (leave blank if no to date):"));
                        panel.add(toDateField);

                        int result = JOptionPane.showConfirmDialog(this, panel, "Update Producer-Company", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION) {
                            String updateSQL = "UPDATE producercompany SET FromDate = ?, ToDate = ? WHERE ProducerID = ? AND RecordCompanyID = ?";
                            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSQL)) {
                                updatePstmt.setString(1, fromDateField.getText());
                                updatePstmt.setObject(2, toDateField.getText().isEmpty() ? null : toDateField.getText()); 
                                updatePstmt.setInt(3, producerID);
                                updatePstmt.setInt(4, companyID);
                                updatePstmt.executeUpdate();
                            }

                            loadProducerCompanyData();
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating producer-company data: " + ex.getMessage());
        }
    }

    private void deleteProducerCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        int producerID = (int) dataTable.getValueAt(selectedRow, 0);
        int companyID = (int) dataTable.getValueAt(selectedRow, 1);

        try {
            String deleteSQL = "DELETE FROM producercompany WHERE ProducerID = ? AND RecordCompanyID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setInt(1, producerID);
                pstmt.setInt(2, companyID);
                pstmt.executeUpdate();
            }

            loadProducerCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting producer-company data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProducerCompanyDatabaseGUI().setVisible(true));
    }
}
