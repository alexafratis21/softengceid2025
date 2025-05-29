import org.junit.Test;
import javax.swing.*;
import java.awt.*;
import static org.junit.Assert.*;

public class ConcertHistoryDatabaseGUITest {

    @Test
    public void testGUIConstructsSuccessfully() {
        ConcertHistoryDatabaseGUI gui = new ConcertHistoryDatabaseGUI();
        assertNotNull("ConcertHistory GUI should be created", gui);
    }

    @Test
    public void testConcertHistoryMainTableExists() {
        ConcertHistoryDatabaseGUI gui = new ConcertHistoryDatabaseGUI();
        JTable table = findFirstJTable(gui);
        assertNotNull("ConcertHistory table should exist", table);
        assertTrue("ConcertHistory table should have columns", table.getColumnCount() > 0);
    }

    @Test
    public void testAllButtonsExistByText() {
        ConcertHistoryDatabaseGUI gui = new ConcertHistoryDatabaseGUI();
        assertTrue("Insert button should exist", findButtonByText(gui, "Insert ConcertHistory"));
        assertTrue("Update button should exist", findButtonByText(gui, "Update ConcertHistory"));
        assertTrue("Delete button should exist", findButtonByText(gui, "Delete ConcertHistory"));
        assertTrue("Search By Criteria button should exist", findButtonByText(gui, "Search By Criteria"));
    }


    private boolean findButtonByText(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton && ((JButton) comp).getText().equals(text)) {
                return true;
            }
            if (comp instanceof Container) {
                if (findButtonByText((Container) comp, text)) return true;
            }
        }
        return false;
    }

    private JTable findFirstJTable(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JScrollPane) {
                Component view = ((JScrollPane) comp).getViewport().getView();
                if (view instanceof JTable) return (JTable) view;
            }
            if (comp instanceof Container) {
                JTable table = findFirstJTable((Container) comp);
                if (table != null) return table;
            }
        }
        return null;
    }
}
