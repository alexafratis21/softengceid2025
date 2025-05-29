
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class GenreDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public GenreDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Genre Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Genre");
        JButton updateButton = new JButton("Update Genre");
        JButton deleteButton = new JButton("Delete Genre");

        insertButton.addActionListener(e -> insertGenreData());
        updateButton.addActionListener(e -> updateGenreData());
        deleteButton.addActionListener(e -> deleteGenreData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadGenreData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadGenreData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM genre");
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
            JOptionPane.showMessageDialog(this, "Error loading genre data: " + ex.getMessage());
        }
    }

    private void insertGenreData() {
        JTextField genreNameField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Genre Name:"));
        panel.add(genreNameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Insert Genre", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String genreName = genreNameField.getText();

            try {
                String insertSQL = "INSERT INTO genre (GenreName) VALUES (?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, genreName);
                pstmt.executeUpdate();

                loadGenreData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error inserting genre data: " + ex.getMessage());
            }
        }
    }

    private void updateGenreData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int genreID = (int) dataTable.getValueAt(selectedRow, 0);
            String genreName = (String) dataTable.getValueAt(selectedRow, 1);

            JTextField genreNameField = new JTextField(genreName);

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Genre Name:"));
            panel.add(genreNameField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Update Genre", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String updateSQL = "UPDATE genre SET GenreName = ? WHERE GenreID = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                pstmt.setString(1, genreNameField.getText());
                pstmt.setInt(2, genreID);
                pstmt.executeUpdate();

                loadGenreData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating genre data: " + ex.getMessage());
        }
    }

    private void deleteGenreData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int genreID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM genre WHERE GenreID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, genreID);
            pstmt.executeUpdate();

            loadGenreData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting genre data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GenreDatabaseGUI().setVisible(true));
    }
}



