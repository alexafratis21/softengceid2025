
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class PersonDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public PersonDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Person Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Person");
        JButton updateButton = new JButton("Update Person");
        JButton deleteButton = new JButton("Delete Person");

        insertButton.addActionListener(e -> insertPersonData());
        updateButton.addActionListener(e -> updatePersonData());
        deleteButton.addActionListener(e -> deletePersonData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadPersonData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }

    private void loadPersonData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM person");
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
            JOptionPane.showMessageDialog(this, "Error loading person data: " + ex.getMessage());
        }
    }

    private void insertPersonData() {
        try {
            String sql = "SELECT ArtistID, ArtistType FROM artist";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            Map<Integer, String> artists = new HashMap<>();
            while (rs.next()) {
                artists.put(rs.getInt("ArtistID"), rs.getString("ArtistType"));
            }

            JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));
            JTextField firstNameField = new JTextField();
            JTextField lastNameField = new JTextField();
            JTextField birthdateField = new JTextField();
            JTextField countryField = new JTextField();
            JTextField aliasField = new JTextField();
            JCheckBox isSoloArtistCheckBox = new JCheckBox();

            JPanel panel = new JPanel(new GridLayout(7, 2));
            panel.add(new JLabel("Select Artist:"));
            panel.add(artistComboBox);
            panel.add(new JLabel("First Name:"));
            panel.add(firstNameField);
            panel.add(new JLabel("Last Name:"));
            panel.add(lastNameField);
            panel.add(new JLabel("Birthdate (YYYY-MM-DD):"));
            panel.add(birthdateField);
            panel.add(new JLabel("Country:"));
            panel.add(countryField);
            panel.add(new JLabel("Alias:"));
            panel.add(aliasField);
            panel.add(new JLabel("Solo Artist:"));
            panel.add(isSoloArtistCheckBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Person", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                Integer artistID = (Integer) artistComboBox.getSelectedItem();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String birthdate = birthdateField.getText();
                String country = countryField.getText();
                String alias = aliasField.getText();
                boolean isSoloArtist = isSoloArtistCheckBox.isSelected();

                String insertSQL = "INSERT INTO person (FirstName, LastName, Birthdate, Country, Alias, isSoloArtist, ArtistID) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, birthdate);
                pstmt.setString(4, country);
                pstmt.setString(5, alias);
                pstmt.setBoolean(6, isSoloArtist);
                pstmt.setInt(7, artistID);
                pstmt.executeUpdate();

                loadPersonData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting person data: " + ex.getMessage());
        }
    }

    private void updatePersonData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int personID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT FirstName, LastName, Birthdate, Country, Alias, isSoloArtist FROM person WHERE PersonID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, personID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("FirstName");
                String lastName = rs.getString("LastName");
                String birthdate = rs.getString("Birthdate");
                String country = rs.getString("Country");
                String alias = rs.getString("Alias");
                boolean isSoloArtist = rs.getBoolean("isSoloArtist");

                JTextField firstNameField = new JTextField(firstName);
                JTextField lastNameField = new JTextField(lastName);
                JTextField birthdateField = new JTextField(birthdate);
                JTextField countryField = new JTextField(country);
                JTextField aliasField = new JTextField(alias);
                JCheckBox isSoloArtistCheckBox = new JCheckBox();
                isSoloArtistCheckBox.setSelected(isSoloArtist);

                JPanel panel = new JPanel(new GridLayout(7, 2));
                panel.add(new JLabel("First Name:"));
                panel.add(firstNameField);
                panel.add(new JLabel("Last Name:"));
                panel.add(lastNameField);
                panel.add(new JLabel("Birthdate:"));
                panel.add(birthdateField);
                panel.add(new JLabel("Country:"));
                panel.add(countryField);
                panel.add(new JLabel("Alias:"));
                panel.add(aliasField);
                panel.add(new JLabel("Solo Artist:"));
                panel.add(isSoloArtistCheckBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Person", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE person SET FirstName = ?, LastName = ?, Birthdate = ?, Country = ?, Alias = ?, isSoloArtist = ? WHERE PersonID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, firstNameField.getText());
                    pstmt.setString(2, lastNameField.getText());
                    pstmt.setString(3, birthdateField.getText());
                    pstmt.setString(4, countryField.getText());
                    pstmt.setString(5, aliasField.getText());
                    pstmt.setBoolean(6, isSoloArtistCheckBox.isSelected());
                    pstmt.setInt(7, personID);
                    pstmt.executeUpdate();

                    loadPersonData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating person data: " + ex.getMessage());
        }
    }

    private void deletePersonData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int personID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM person WHERE PersonID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, personID);
            pstmt.executeUpdate();

            loadPersonData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting person data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PersonDatabaseGUI().setVisible(true));
    }
}


