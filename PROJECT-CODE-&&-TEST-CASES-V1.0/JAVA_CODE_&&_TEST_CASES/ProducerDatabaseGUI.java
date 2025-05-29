
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ProducerDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public ProducerDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Producer Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Producer");
        JButton updateButton = new JButton("Update Producer");
        JButton deleteButton = new JButton("Delete Producer");

        insertButton.addActionListener(e -> insertProducerData());
        updateButton.addActionListener(e -> updateProducerData());
        deleteButton.addActionListener(e -> deleteProducerData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadProducerData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
private void loadProducerData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM producer");
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
            JOptionPane.showMessageDialog(this, "Error loading producer data: " + ex.getMessage());
        }
    }

    private void insertProducerData() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField numOfProdAlbumsField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Number of Produced Albums:"));
        panel.add(numOfProdAlbumsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert Producer", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            int numOfProdAlbums = Integer.parseInt(numOfProdAlbumsField.getText());

            String insertSQL = "INSERT INTO producer (FirstName, LastName, NumofProdAlbums) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setInt(3, numOfProdAlbums);
                pstmt.executeUpdate();
                loadProducerData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting producer data: " + ex.getMessage());
            }
        }
    }

    private void updateProducerData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        int producerID = (int) dataTable.getValueAt(selectedRow, 0);

        try {
            String sql = "SELECT FirstName, LastName, NumofProdAlbums FROM producer WHERE ProducerID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, producerID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                int numOfProdAlbums = rs.getInt("NumofProdAlbums");

                JTextField firstNameField = new JTextField(firstName);
                JTextField lastNameField = new JTextField(lastName);
                JTextField numOfProdAlbumsField = new JTextField(String.valueOf(numOfProdAlbums));

                JPanel panel = new JPanel(new GridLayout(4, 2));
                panel.add(new JLabel("First Name:"));
                panel.add(firstNameField);
                panel.add(new JLabel("Last Name:"));
                panel.add(lastNameField);
                panel.add(new JLabel("Number of Produced Albums:"));
                panel.add(numOfProdAlbumsField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Producer", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE producer SET FirstName = ?, LastName = ?, NumofProdAlbums = ? WHERE ProducerID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, firstNameField.getText());
                    pstmt.setString(2, lastNameField.getText());
                    pstmt.setInt(3, Integer.parseInt(numOfProdAlbumsField.getText()));
                    pstmt.setInt(4, producerID);
                    pstmt.executeUpdate();

                    loadProducerData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating producer data: " + ex.getMessage());
        }
    }

    private void deleteProducerData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        int producerID = (int) dataTable.getValueAt(selectedRow, 0);

        try {
            String deleteSQL = "DELETE FROM producer WHERE ProducerID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, producerID);
            pstmt.executeUpdate();

            loadProducerData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting producer data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProducerDatabaseGUI().setVisible(true));
    }
}

