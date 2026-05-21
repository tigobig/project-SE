package data_management;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

/**
 * Tests for DataStorage: adding records, retrieving by time range, multiple patients.
 */
class DataStorageTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
    }

    // ----- basic add + get -----

    @Test
    void testAddAndGetRecords() {
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsForNonExistentPatient() {
        List<PatientRecord> records = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    //time range filtering 

    @Test
    void testGetRecordsTimeRangeFiltersCorrectly() {
        storage.addPatientData(1, 70.0, "HeartRate", 1000L);
        storage.addPatientData(1, 75.0, "HeartRate", 2000L);
        storage.addPatientData(1, 80.0, "HeartRate", 3000L);

        List<PatientRecord> records = storage.getRecords(1, 1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetRecordsBoundaryTimestampsAreInclusive() {
        storage.addPatientData(1, 70.0, "HeartRate", 1000L);
        storage.addPatientData(1, 80.0, "HeartRate", 2000L);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 2000L);
        assertEquals(2, records.size());
    }

    @Test
    void testGetRecordsReturnsEmptyWhenNoneInRange() {
        storage.addPatientData(1, 70.0, "HeartRate", 1000L);

        List<PatientRecord> records = storage.getRecords(1, 5000L, 9000L);
        assertTrue(records.isEmpty());
    }

    //multiple patients 

    @Test
    void testMultiplePatientsStoredIndependently() {
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(2, 200.0, "HeartRate", 1000L);

        assertEquals(1, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0L, Long.MAX_VALUE).size());
        assertEquals(2, storage.getAllPatients().size());
    }

    @Test
    void testGetAllPatientsReturnsAllAdded() {
        storage.addPatientData(1, 100.0, "ECG", 1000L);
        storage.addPatientData(2, 110.0, "ECG", 1000L);
        storage.addPatientData(3, 120.0, "ECG", 1000L);

        assertEquals(3, storage.getAllPatients().size());
    }

    //multiple record types 

    @Test
    void testAddMultipleRecordTypesForSamePatient() {
        storage.addPatientData(1, 120.0, "SystolicPressure",  1000L);
        storage.addPatientData(1, 80.0,  "DiastolicPressure", 1000L);
        storage.addPatientData(1, 97.0,  "Saturation",        1000L);

        List<PatientRecord> all = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(3, all.size());
    }

    @Test
    void testRecordFieldsStoredCorrectly() {
        storage.addPatientData(5, 55.5, "Saturation", 999L);

        List<PatientRecord> records = storage.getRecords(5, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
        PatientRecord r = records.get(0);
        assertEquals(5,       r.getPatientId());
        assertEquals(55.5,    r.getMeasurementValue());
        assertEquals("Saturation", r.getRecordType());
        assertEquals(999L,    r.getTimestamp());
    }
}
