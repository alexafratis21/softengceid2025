import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ArtistCompanyDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public ArtistCompanyDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("ArtistCompany Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert ArtistCompany");
        JButton updateButton = new JButton("Update ArtistCompany");
        JButton deleteButton = new JButton("Delete ArtistCompany");

        insertButton.addActionListener(e -> insertArtistCompanyData());
        updateButton.addActionListener(e -> updateArtistCompanyData());
        deleteButton.addActionListener(e -> deleteArtistCompanyData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadArtistCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadArtistCompanyData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM artistcompany");
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
            JOptionPane.showMessageDialog(this, "Error loading artistcompany data: " + ex.getMessage());
        }
    }

    private void insertArtistCompanyData() {
        try {
            String artistQuery = "SELECT ArtistID, ArtistType FROM artist";
            Statement stmt = conn.createStatement();
            ResultSet rsArtist = stmt.executeQuery(artistQuery);
            Map<Integer, String> artists = new HashMap<>();
            while (rsArtist.next()) {
                artists.put(rsArtist.getInt("ArtistID"), rsArtist.getString("ArtistType"));
            }

            String companyQuery = "SELECT CompanyID, CompanyName FROM recordcompany";
            ResultSet rsCompany = stmt.executeQuery(companyQuery);
            Map<Integer, String> companies = new HashMap<>();
            while (rsCompany.next()) {
                companies.put(rsCompany.getInt("CompanyID"), rsCompany.getString("CompanyName"));
            }

            JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));
            JComboBox<Integer> companyComboBox = new JComboBox<>(companies.keySet().toArray(new Integer[0]));
            JTextField fromDateField = new JTextField();
            JTextField toDateField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Select Artist:"));
            panel.add(artistComboBox);
            panel.add(new JLabel("Select Company:"));
            panel.add(companyComboBox);
            panel.add(new JLabel("From Date (YYYY-MM-DD):"));
            panel.add(fromDateField);
            panel.add(new JLabel("To Date (YYYY-MM-DD or leave blank):"));
            panel.add(toDateField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert ArtistCompany", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer artistID = (Integer) artistComboBox.getSelectedItem();
                Integer companyID = (Integer) companyComboBox.getSelectedItem();
                String fromDate = fromDateField.getText();
                String toDate = toDateField.getText().isEmpty() ? null : toDateField.getText(); 

                String insertSQL = "INSERT INTO artistcompany (ArtistID, CompanyID, FromDate, ToDate) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setInt(1, artistID);
                pstmt.setInt(2, companyID);
                pstmt.setString(3, fromDate);
                pstmt.setObject(4, toDate); 
                pstmt.executeUpdate();

                loadArtistCompanyData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting artistcompany data: " + ex.getMessage());
        }
    }

    private void updateArtistCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int artistID = (int) dataTable.getValueAt(selectedRow, 0);
            int companyID = (int) dataTable.getValueAt(selectedRow, 1);

            String sql = "SELECT FromDate, ToDate FROM artistcompany WHERE ArtistID = ? AND CompanyID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, artistID);
            pstmt.setInt(2, companyID);
            ResultSet rs = pstmt.executeQuery();

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

                int result = JOptionPane.showConfirmDialog(this, panel, "Update ArtistCompany", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE artistcompany SET FromDate = ?, ToDate = ? WHERE ArtistID = ? AND CompanyID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, fromDateField.getText());
                    pstmt.setObject(2, toDateField.getText().isEmpty() ? null : toDateField.getText()); 
                    pstmt.setInt(3, artistID);
                    pstmt.setInt(4, companyID);
                    pstmt.executeUpdate();

                    loadArtistCompanyData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating artistcompany data: " + ex.getMessage());
        }
    }

    private void deleteArtistCompanyData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int artistID = (int) dataTable.getValueAt(selectedRow, 0);
            int companyID = (int) dataTable.getValueAt(selectedRow, 1);

            String deleteSQL = "DELETE FROM artistcompany WHERE ArtistID = ? AND CompanyID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, artistID);
            pstmt.setInt(2, companyID);
            pstmt.executeUpdate();

            loadArtistCompanyData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting artistcompany data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ArtistCompanyDatabaseGUI().setVisible(true));
    }
}
