
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class LogDatabaseGUI extends JPanel {
    private Connection conn;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public LogDatabaseGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Log Table Operations"));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton insertButton = new JButton("Insert Log");
        JButton updateButton = new JButton("Update Log");
        JButton deleteButton = new JButton("Delete Log");

        insertButton.addActionListener(e -> insertLogData());
        updateButton.addActionListener(e -> updateLogData());
        deleteButton.addActionListener(e -> deleteLogData());

        actionPanel.add(insertButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        add(actionPanel, BorderLayout.SOUTH);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recordcompany", "root", "root");
            loadLogData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + ex.getMessage());
        }
    }
    
    private void loadLogData() {
        try {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            Statement stmt = conn.createStatement();  
            ResultSet rs = stmt.executeQuery("SELECT * FROM log");
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
            JOptionPane.showMessageDialog(this, "Error loading log data: " + ex.getMessage());
        }
    }

    private void insertLogData() {
        try {
            String[] tableNames = {"person", "band", "album", "concert", "venue"};  
            JComboBox<String> tableComboBox = new JComboBox<>(tableNames);

            String[] actionTypes = {"INSERT", "UPDATE", "DELETE"};
            JComboBox<String> actionComboBox = new JComboBox<>(actionTypes);

            JTextField dateTimeField = new JTextField(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

            JComboBox<Integer> dbaComboBox = new JComboBox<>();
            String dbaQuery = "SELECT DBAID, Username FROM dba";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(dbaQuery);
            while (rs.next()) {
                dbaComboBox.addItem(rs.getInt("DBAID"));
            }

            JPanel panel = new JPanel(new GridLayout(5, 2));  
            panel.add(new JLabel("Table Name:"));
            panel.add(tableComboBox);
            panel.add(new JLabel("Action Type:"));
            panel.add(actionComboBox);
            panel.add(new JLabel("Action DateTime:"));
            panel.add(dateTimeField);
            panel.add(new JLabel("DBA:"));
            panel.add(dbaComboBox);

            int result = JOptionPane.showConfirmDialog(this, panel, "Insert Log", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String tableName = (String) tableComboBox.getSelectedItem();
                String actionType = (String) actionComboBox.getSelectedItem();
                String actionDateTime = dateTimeField.getText();
                Integer dbaID = (Integer) dbaComboBox.getSelectedItem();

                String insertSQL = "INSERT INTO log (TableName, ActionType, ActionDateTime, DBAID) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, tableName);
                pstmt.setString(2, actionType);
                pstmt.setString(3, actionDateTime);
                pstmt.setInt(4, dbaID);
                pstmt.executeUpdate();

                loadLogData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting log data: " + ex.getMessage());
        }
    }

    private void updateLogData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        try {
            int logID = (int) dataTable.getValueAt(selectedRow, 0);

            String sql = "SELECT TableName, ActionType, ActionDateTime, DBAID FROM log WHERE LogID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, logID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String tableName = rs.getString("TableName");
                String actionType = rs.getString("ActionType");
                String actionDateTime = rs.getString("ActionDateTime");
                int dbaID = rs.getInt("DBAID");

                JComboBox<String> tableComboBox = new JComboBox<>(new String[]{"person", "band", "album", "concert", "venue"});
                JComboBox<String> actionComboBox = new JComboBox<>(new String[]{"INSERT", "UPDATE", "DELETE"});
                JTextField dateTimeField = new JTextField(actionDateTime);
                JComboBox<Integer> dbaComboBox = new JComboBox<>();
                String dbaQuery = "SELECT DBAID, Username FROM dba";
                Statement stmt = conn.createStatement();
                ResultSet dbaRS = stmt.executeQuery(dbaQuery);
                while (dbaRS.next()) {
                    dbaComboBox.addItem(dbaRS.getInt("DBAID"));
                }

                JPanel panel = new JPanel(new GridLayout(5, 2)); 
                panel.add(new JLabel("Table Name:"));
                panel.add(tableComboBox);
                panel.add(new JLabel("Action Type:"));
                panel.add(actionComboBox);
                panel.add(new JLabel("Action DateTime:"));
                panel.add(dateTimeField);
                panel.add(new JLabel("DBA:"));
                panel.add(dbaComboBox);

                int result = JOptionPane.showConfirmDialog(this, panel, "Update Log", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String updateSQL = "UPDATE log SET TableName = ?, ActionType = ?, ActionDateTime = ?, DBAID = ? WHERE LogID = ?";
                    pstmt = conn.prepareStatement(updateSQL);
                    pstmt.setString(1, (String) tableComboBox.getSelectedItem());
                    pstmt.setString(2, (String) actionComboBox.getSelectedItem());
                    pstmt.setString(3, dateTimeField.getText());
                    pstmt.setInt(4, (Integer) dbaComboBox.getSelectedItem());
                    pstmt.setInt(5, logID);
                    pstmt.executeUpdate();

                    loadLogData();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating log data: " + ex.getMessage());
        }
    }

    private void deleteLogData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }

        try {
            int logID = (int) dataTable.getValueAt(selectedRow, 0);

            String deleteSQL = "DELETE FROM log WHERE LogID = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            pstmt.setInt(1, logID);
            pstmt.executeUpdate();

            loadLogData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting log data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LogDatabaseGUI().setVisible(true));
    }
}
