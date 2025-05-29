import javax.swing.*;
import java.awt.*;

public class DatabaseGUI extends JFrame {
    private JPanel contentPanel;  
    private CardLayout cardLayout;  

    public DatabaseGUI() {
        setTitle("Rhythm Registry");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new FlowLayout());
        JComboBox<String> tableSelector = new JComboBox<>(new String[]{"Artist", "Person", "Band", "BandMember", "RecordCompany", "ArtistCompany", "Producer", "ProducerCompany", "Genre", "Album", "AlbumRelease", "Track", "Venue", "Concert", "ConcertHistory", "DBA", "Log"}); 
        tableSelector.addActionListener(e -> loadTableGUI((String) tableSelector.getSelectedItem()));
        northPanel.add(new JLabel("Select Table:"));
        northPanel.add(tableSelector);
        add(northPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        add(contentPanel, BorderLayout.CENTER);

        loadTableGUI("Artist");
    }
    
    public JPanel getContentPanel() {
    return contentPanel;
}

    public void loadTableGUI(String tableName) {
        contentPanel.removeAll();  

        JPanel tablePanel = null;
        switch (tableName) {
            case "Artist":
                tablePanel = new ArtistDatabaseGUI();  
                break;
            case "Person":
                tablePanel = new PersonDatabaseGUI(); 
                break;
            case "Band":
                tablePanel = new BandDatabaseGUI(); 
                break;
            case "BandMember":
                tablePanel = new BandMemberDatabaseGUI(); 
                break;
            case "RecordCompany":
                tablePanel = new RecordCompanyDatabaseGUI(); 
                break;
            case "ArtistCompany":
                tablePanel = new ArtistCompanyDatabaseGUI(); 
                break;
            case "Producer":
                tablePanel = new ProducerDatabaseGUI(); 
                break;
            case "ProducerCompany":
                tablePanel = new ProducerCompanyDatabaseGUI(); 
                break;
            case "Genre":
                tablePanel = new GenreDatabaseGUI(); 
                break;
            case "Album":
                tablePanel = new AlbumDatabaseGUI(); 
                break;
            case "AlbumRelease":
                tablePanel = new AlbumReleaseDatabaseGUI(); 
                break;
            case "Track":
                tablePanel = new TrackDatabaseGUI(); 
                break;
            case "Venue":
                tablePanel = new VenueDatabaseGUI(); 
                break;
            case "Concert":
                tablePanel = new ConcertDatabaseGUI(); 
                break;
            case "ConcertHistory":
                tablePanel = new ConcertHistoryDatabaseGUI(); 
                break;
            case "DBA":
                tablePanel = new DBADatabaseGUI(); 
                break;
            case "Log":
                tablePanel = new LogDatabaseGUI(); 
                break; 
            default:
                JOptionPane.showMessageDialog(this, "Unknown table: " + tableName);
        }

        if (tablePanel != null) {
            contentPanel.add(tablePanel, tableName);  
        }

        cardLayout.show(contentPanel, tableName);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DatabaseGUI().setVisible(true));
    }
}




