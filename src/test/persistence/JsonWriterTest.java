package persistence;

import model.VisitorEntry;
import model.VisitorLog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


// Citation: Structure adapted from the CPSC210 JsonSerializationDemo.
public class JsonWriterTest extends JsonTest {

    @Test
    void testWriterInvalidFile() {
        try {
            JsonWriter writer = new JsonWriter("./data/my\0illegal:fileName.json");
            writer.open();
            fail("IOException expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testWriterEmptyVisitorLog() {
        try {
            VisitorLog log = new VisitorLog();
            JsonWriter writer = new JsonWriter("./data/testWriterEmptyVisitorLog.json");
            writer.open();
            writer.write(log);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterEmptyVisitorLog.json");
            log = reader.read();
            assertEquals(0, log.getTotalEntries());
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriterGeneralVisitorLog() {
        try {
            VisitorLog log = new VisitorLog();
            VisitorEntry e1 = new VisitorEntry("Bob", "Resident X",
                    LocalDateTime.parse("2025-10-09T10:00:00"));
            e1.signOut(LocalDateTime.parse("2025-10-09T10:45:00"));
            VisitorEntry e2 = new VisitorEntry("VisitorY", "Resident Y",
                    LocalDateTime.parse("2025-10-09T11:15:00"));
            log.addEntry(e1);
            log.addEntry(e2);

            JsonWriter writer = new JsonWriter("./data/testWriterGeneralVisitorLog.json");
            writer.open();
            writer.write(log);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterGeneralVisitorLog.json");
            log = reader.read();
            List<VisitorEntry> entries = log.getAllEntries();
            assertEquals(2, entries.size());
            checkVisitorEntry("Bob", "Resident X", e1.getTimeIn(), e1.getTimeOut(), entries.get(0));
            checkVisitorEntry("VisitorY", "Resident Y", e2.getTimeIn(), e2.getTimeOut(), entries.get(1));
        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriterCloseWithoutOpen() {
        JsonWriter writer = new JsonWriter("./data/testWriterCloseWithoutOpen.json");
        writer.close();
    }

}
