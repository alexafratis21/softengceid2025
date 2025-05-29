import org.junit.Test;
import javax.swing.*;
import static org.junit.Assert.*;
import java.awt.Container;
import java.awt.Component;

public class ArtistDatabaseGUITest {

    @Test
    public void testGUIConstructionLoadsData() {
        ArtistDatabaseGUI gui = new ArtistDatabaseGUI();

        assertNotNull(gui);

        JTable table = findJTable(gui);
        assertNotNull(table);

        assertTrue(table.getColumnCount() > 0);
    }
    
@Test
public void testButtonsExist() {
    ArtistDatabaseGUI gui = new ArtistDatabaseGUI();
    boolean foundInsert = findButton(gui, "Insert Artist");
    boolean foundUpdate = findButton(gui, "Update Artist");
    boolean foundDelete = findButton(gui, "Delete Artist");

    assertTrue(foundInsert && foundUpdate && foundDelete);
}

private boolean findButton(Container container, String text) {
    for (Component comp : container.getComponents()) {
        if (comp instanceof JButton) {
            if (((JButton) comp).getText().equals(text)) return true;
        }
        if (comp instanceof Container) {
            if (findButton((Container) comp, text)) return true;
        }
    }
    return false;
}


    private JTable findJTable(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) return (JTable) comp;
            if (comp instanceof Container) {
                JTable result = findJTable((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }
}


