package data_management;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.PatientRecord;

/**
 * Tests for FileDataReader: parsing valid records, edge cases, and error handling.
 */
class FileDataReaderTest {

    @TempDir
    Path tempDir;

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    //valid data

    @Test
    void testReadValidNumericRecord() throws IOException {
        writeLines(tempDir.resolve("HeartRate.txt"),
            "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0");

        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals("HeartRate", records.get(0).getRecordType());
    }

    @Test
    void testReadMultipleRecordsFromOneFile() throws IOException {
        writeLines(tempDir.resolve("HeartRate.txt"),
            "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0",
            "Patient ID: 1, Timestamp: 2000, Label: HeartRate, Data: 75.0");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(2, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testReadSaturationWithPercentSignStripped() throws IOException {
        writeLines(tempDir.resolve("Saturation.txt"),
            "Patient ID: 2, Timestamp: 1000, Label: Saturation, Data: 97.0%");

        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(2, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(97.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testReadAlertTriggeredMapsToOne() throws IOException {
        writeLines(tempDir.resolve("Alert.txt"),
            "Patient ID: 3, Timestamp: 1000, Label: Alert, Data: triggered");

        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(3, 0L, Long.MAX_VALUE);
        assertEquals(1.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testReadAlertResolvedMapsToZero() throws IOException {
        writeLines(tempDir.resolve("Alert.txt"),
            "Patient ID: 3, Timestamp: 1000, Label: Alert, Data: resolved");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(0.0, storage.getRecords(3, 0L, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testReadMultiplePatientsFromMultipleFiles() throws IOException {
        writeLines(tempDir.resolve("file1.txt"),
            "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 0.5");
        writeLines(tempDir.resolve("file2.txt"),
            "Patient ID: 2, Timestamp: 1000, Label: ECG, Data: 0.6");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(1, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testTimestampStoredCorrectly() throws IOException {
        writeLines(tempDir.resolve("bp.txt"),
            "Patient ID: 1, Timestamp: 1714376789050, Label: SystolicPressure, Data: 120.0");

        new FileDataReader(tempDir.toString()).readData(storage);

        PatientRecord r = storage.getRecords(1, 0L, Long.MAX_VALUE).get(0);
        assertEquals(1714376789050L, r.getTimestamp());
    }

    //error handling 

    @Test
    void testNonExistentDirectoryThrowsIOException() {
        FileDataReader reader = new FileDataReader("/definitely/does/not/exist");
        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    @Test
    void testMalformedLineIsSkippedAndValidLinesStillParsed() throws IOException {
        writeLines(tempDir.resolve("mixed.txt"),
            "this is not a valid line at all",
            "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(1, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testEmptyFileResultsInNoRecords() throws IOException {
        Files.createFile(tempDir.resolve("empty.txt"));

        new FileDataReader(tempDir.toString()).readData(storage);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testNonTxtFilesAreIgnored() throws IOException {
        writeLines(tempDir.resolve("data.csv"),
            "Patient ID: 1, Timestamp: 1000, Label: HeartRate, Data: 72.0");

        new FileDataReader(tempDir.toString()).readData(storage);

        // .csv is not a .txt file should be ignored
        assertTrue(storage.getAllPatients().isEmpty());
    }

    //helper 

    private void writeLines(Path file, String... lines) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            for (String line : lines) {
                w.println(line);
            }
        }
    }
}
