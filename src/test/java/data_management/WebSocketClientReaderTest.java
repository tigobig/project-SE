package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketClientReaderTest {

    private DataStorage storage;
    private WebSocketClientReader reader;

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        reader = new WebSocketClientReader(new URI("ws://localhost:19998"));
        reader.setDataStorage(storage);
    }

    @Test
    void testValidNumericMessageStoresRecord() {
        reader.parseAndStore("1,1000,HeartRate,72.0");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
        assertEquals(1000L, records.get(0).getTimestamp());
    }

    @Test
    void testSaturationPercentSignStripped() {
        reader.parseAndStore("2,1000,Saturation,97.0%");

        List<PatientRecord> records = storage.getRecords(2, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(97.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testTriggeredMapsToOne() {
        reader.parseAndStore("3,1000,Alert,triggered");

        List<PatientRecord> records = storage.getRecords(3, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(1.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testResolvedMapsToZero() {
        reader.parseAndStore("3,1000,Alert,resolved");

        List<PatientRecord> records = storage.getRecords(3, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(0.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testMalformedMessageSkippedNoRecord() {
        reader.parseAndStore("not valid");

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testTooFewFieldsSkipped() {
        reader.parseAndStore("1,1000,HeartRate");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testMultipleMessagesStoredCorrectly() {
        reader.parseAndStore("1,1000,HeartRate,72.0");
        reader.parseAndStore("2,2000,HeartRate,80.0");

        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testTimestampStoredCorrectly() {
        long ts = 1714376789050L;
        reader.parseAndStore("1," + ts + ",HeartRate,72.0");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(ts, records.get(0).getTimestamp());
    }

    @Test
    void testPatientIdStoredCorrectly() {
        reader.parseAndStore("42,1000,HeartRate,72.0");

        List<PatientRecord> records = storage.getRecords(42, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(42, records.get(0).getPatientId());
    }

    @Test
    void testNullDataStorageDropsMessage() throws Exception {
        WebSocketClientReader readerNoStorage = new WebSocketClientReader(new URI("ws://localhost:19998"));
        assertDoesNotThrow(() -> readerNoStorage.onMessage("1,1000,HeartRate,72.0"));
    }

    @Test
    void testReadDataThrowsIOExceptionForUnreachableServer() throws Exception {
        WebSocketClientReader unreachable = new WebSocketClientReader(new URI("ws://localhost:19998"));
        assertThrows(IOException.class, () -> unreachable.readData(storage));
    }
}
