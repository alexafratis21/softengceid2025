
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class AlbumDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public AlbumDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Album Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Album");
        JButton updateButton = new JButton("Update Album");
        JButton deleteButton = new JButton("Delete Album");

        insertButton.addActionListener(e -> insertAlbumData());
        updateButton.addActionListener(e -> updateAlbumData());
        deleteButton.addActionListener(e -> deleteAlbumData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadAlbumData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
   private void loadAlbumData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM album");
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
            JOptionPane.showMessageDialog(this, "Error loading album data: " + ex.getMessage());
        }
    }

    private void insertAlbumData() {
        try {
            String artistQuery = "SELECT ArtistID, ArtistType FROM artist";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(artistQuery);
            Map<Integer, String> artists = new HashMap<>();
            while (rs.next()) {
                artists.put(rs.getInt("ArtistID"), rs.getString("ArtistType"));
            }

            String genreQuery = "SELECT GenreID, GenreName FROM genre";
            rs = stmt.executeQuery(genreQuery);
            Map<Integer, String> genres = new HashMap<>();
            while (rs.next()) {
                genres.put(rs.getInt("GenreID"), rs.getString("GenreName"));
            }

            String companyQuery = "SELECT CompanyID, CompanyName FROM recordcompany";
            rs = stmt.executeQuery(companyQuery);
            Map<Integer, String> companies = new HashMap<>();
            while (rs.next()) {
                companies.put(rs.getInt("CompanyID"), rs.getString("CompanyName"));
            }

            String producerQuery = "SELECT ProducerID, FirstName, LastName FROM producer";
            rs = stmt.executeQuery(producerQuery);
            Map<Integer, String> producers = new HashMap<>();
            while (rs.next()) {
                producers.put(rs.getInt("ProducerID"), rs.getString("FirstName") + " " + rs.getString("LastName"));
            }

            JComboBox<Integer> artistComboBox = new JComboBox<>(artists.keySet().toArray(new Integer[0]));
            JComboBox<Integer> genreComboBox = new JComboBox<>(genres.keySet().toArray(new Integer[0]));
            JComboBox<Integer> companyComboBox = new JComboBox<>(companies.keySet().toArray(new Integer[0]));
            JComboBox<Integer> producerComboBox = new JComboBox<>(producers.keySet().toArray(new Integer[0]));

            JTextField albumTitleField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(5, 2));
            panel.add(new JLabel("Album Title:"));
            panel.add(albumTitleField);
            panel.add(new JLabel("Select Artist:"));
            panel.add(artistComboBox);
            panel.add(new JLabel("Select Genre:"));
            panel.add(genreComboBox);
            panel.add(new JLabel("Select Record Company:"));
            panel.add(companyComboBox);
            panel.add(new JLabel("Select Producer:"));
            panel.add(producerComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Album", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String title = albumTitleField.getText();
                Integer artistID = (Integer) artistComboBox.getSelectedItem();
                Integer genreID = (Integer) genreComboBox.getSelectedItem();
                Integer companyID = (Integer) companyComboBox.getSelectedItem();
                Integer producerID = (Integer) producerComboBox.getSelectedItem();

                String insertSQL = "INSERT INTO album (Title, ArtistID, GenreID, CompanyID, ProducerID) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, title);
                pstmt.setInt(2, artistID);
                pstmt.setInt(3, genreID);
                pstmt.setInt(4, companyID);
                pstmt.setInt(5, producerID);
                pstmt.executeUpdate();

                loadAlbumData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting album data: " + ex.getMessage());
        }
    }

    private void updateAlbumData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int albumID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT Title, ArtistID, GenreID, CompanyID, ProducerID FROM album WHERE AlbumID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, albumID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String title = rs.getString("Title");
                int artistID = rs.getInt("ArtistID");
                int genreID = rs.getInt("GenreID");
                int companyID = rs.getInt("CompanyID");
                int producerID = rs.getInt("ProducerID");

                JTextField albumTitleField = new JTextField(title);
                JComboBox<Integer> artistComboBox = new JComboBox<>(new Integer[] { artistID });
                JComboBox<Integer> genreComboBox = new JComboBox<>(new Integer[] { genreID });
                JComboBox<Integer> companyComboBox = new JComboBox<>(new Integer[] { companyID });
                JComboBox<Integer> producerComboBox = new JComboBox<>(new Integer[] { producerID });

                JPanel panel = new JPanel(new GridLayout(5, 2));
                panel.add(new JLabel("Album Title:"));
                panel.add(albumTitleField);
                panel.add(new JLabel("Select Artist:"));
                panel.add(artistComboBox);
                panel.add(new JLabel("Select Genre:"));
                panel.add(genreComboBox);
                panel.add(new JLabel("Select Record Company:"));
                panel.add(companyComboBox);
                panel.add(new JLabel("Select Producer:"));
                panel.add(producerComboBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Album", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE album SET Title = ?, ArtistID = ?, GenreID = ?, CompanyID = ?, ProducerID = ? WHERE AlbumID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, albumTitleField.getText());
                    pstmt.setInt(2, (Integer) artistComboBox.getSelectedItem());
                    pstmt.setInt(3, (Integer) genreComboBox.getSelectedItem());
                    pstmt.setInt(4, (Integer) companyComboBox.getSelectedItem());
                    pstmt.setInt(5, (Integer) producerComboBox.getSelectedItem());
                    pstmt.setInt(6, albumID);
                    pstmt.executeUpdate();

                    loadAlbumData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating album data: " + ex.getMessage());
        }
    }

    private void deleteAlbumData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int albumID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM album WHERE AlbumID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, albumID);
            pstmt.executeUpdate();

            loadAlbumData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting album data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlbumDatabaseGUI().setVisible(true));
    }
}

