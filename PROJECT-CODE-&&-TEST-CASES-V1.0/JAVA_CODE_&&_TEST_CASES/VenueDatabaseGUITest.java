import org.junit.Test;
import javax.swing.*;
import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;

public class VenueDatabaseGUITest {

    @Test
    public void testVenueGUIConstructsSuccessfully() {
        VenueDatabaseGUI gui = new VenueDatabaseGUI();
        assertNotNull("GUI should be created", gui);
    }

    @Test
    public void testMainTableIsPresentAndHasColumns() {
        VenueDatabaseGUI gui = new VenueDatabaseGUI();
        JTable dataTable = findFirstJTable(gui);
        assertNotNull("Main JTable should exist", dataTable);
        assertTrue("Main table should have columns", dataTable.getColumnCount() > 0);
    }


    @Test
    public void testButtonsExistByText() {
        VenueDatabaseGUI gui = new VenueDatabaseGUI();

        assertTrue("Insert Venue button exists", findButton(gui, "Insert Venue"));
        assertTrue("Update Venue button exists", findButton(gui, "Update Venue"));
        assertTrue("Delete Venue button exists", findButton(gui, "Delete Venue"));
        assertTrue("Venue Scores button exists", findButton(gui, "Venue Scores"));
    }

    private JTable findFirstJTable(Container container) {
        int count = 0;
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                if (count == 0) return (JTable) comp;
            }
            if (comp instanceof Container) {
                JTable inner = findFirstJTable((Container) comp);
                if (inner != null) return inner;
            }
        }
        return null;
    }

    private JTable findSecondJTable(Container container) {
        int count = 0;
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                if (count == 1) return (JTable) comp;
                count++;
            }
            if (comp instanceof Container) {
                JTable inner = findSecondJTable((Container) comp);
                if (inner != null) return inner;
            }
        }
        return null;
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
}
