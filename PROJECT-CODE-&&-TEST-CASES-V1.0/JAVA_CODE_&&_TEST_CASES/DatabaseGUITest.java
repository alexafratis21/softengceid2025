import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Component;

import javax.swing.*;

public class DatabaseGUITest {

    @Test
    public void testLoadArtistPanel() {
        DatabaseGUI gui = new DatabaseGUI();
        gui.loadTableGUI("Artist");

        JPanel content = gui.getContentPanel();
        assertEquals(1, content.getComponentCount());

        Component panel = content.getComponent(0);
        assertTrue(panel instanceof ArtistDatabaseGUI);
    }

    @Test
    public void testLoadInvalidTableDoesNotCrash() {
        DatabaseGUI gui = new DatabaseGUI();

        gui.loadTableGUI("InvalidTable");

        assertEquals(0, gui.getContentPanel().getComponentCount());
    }
}
