package persistence;

import model.VisitorLog;
import model.VisitorEntry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


// Citation: Structure adapted from the CPSC210 JsonSerializationDemo.
public class JsonReaderTest extends JsonTest {

    @Test
    void testReaderNonExistentFile() {
        JsonReader reader = new JsonReader("./data/noSuchFile.json");
        try {
            VisitorLog log = reader.read();
            fail("IOException expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testReaderEmptyVisitorLog() {
        JsonReader reader = new JsonReader("./data/testReaderEmptyVisitorLog.json");
        try {
            VisitorLog log = reader.read();
            assertEquals(0, log.getTotalEntries());
        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

    @Test
    void testReaderGeneralVisitorLog() {
        JsonReader reader = new JsonReader("./data/testReaderGeneralVisitorLog.json");
        try {
            VisitorLog log = reader.read();
            List<VisitorEntry> entries = log.getAllEntries();
            assertEquals(3, entries.size());

            LocalDateTime t1In = LocalDateTime.parse("2025-10-09T14:30:00");
            LocalDateTime t1Out = LocalDateTime.parse("2025-10-09T15:00:00");
            checkVisitorEntry("Callum", "Resident A", t1In, t1Out, entries.get(0));

            LocalDateTime t2In = LocalDateTime.parse("2025-10-09T16:00:00");
            LocalDateTime t2Out = LocalDateTime.parse("2025-10-09T17:15:00");
            checkVisitorEntry("Visitor1", "Resident B", t2In, t2Out, entries.get(1));

            LocalDateTime t3In = LocalDateTime.parse("2025-10-09T17:30:00");
            checkVisitorEntry("Visitor2", "Resident C", t3In, null, entries.get(2));

        } catch (IOException e) {
            fail("Couldn't read from file");
        }
    }

}
