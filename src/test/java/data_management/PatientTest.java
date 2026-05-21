package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

/**
 * Tests for Patient: getPatientId, addRecord, and getRecords with various time ranges.
 */
class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(42);
    }

    @Test
    void testGetPatientId() {
        assertEquals(42, patient.getPatientId());
    }

    @Test
    void testGetRecordsInRange() {
        patient.addRecord(70.0, "HeartRate", 1000L);
        patient.addRecord(75.0, "HeartRate", 2000L);
        patient.addRecord(80.0, "HeartRate", 3000L);

        List<PatientRecord> records = patient.getRecords(1500L, 2500L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue());
    }

    @Test
    void testGetAllRecordsWithMaxRange() {
        patient.addRecord(70.0, "HeartRate", 1000L);
        patient.addRecord(75.0, "HeartRate", 2000L);

        assertEquals(2, patient.getRecords(0L, Long.MAX_VALUE).size());
    }

    @Test
    void testGetRecordsEmptyList() {
        assertTrue(patient.getRecords(0L, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void testGetRecordsOutsideRangeReturnsEmpty() {
        patient.addRecord(70.0, "HeartRate", 1000L);

        assertTrue(patient.getRecords(2000L, 3000L).isEmpty());
    }

    @Test
    void testGetRecordsBoundaryTimestampsInclusive() {
        patient.addRecord(70.0, "HeartRate", 1000L);
        patient.addRecord(80.0, "HeartRate", 2000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);
        assertEquals(2, records.size());
    }

    @Test
    void testMultipleRecordTypes() {
        patient.addRecord(120.0, "SystolicPressure",  1000L);
        patient.addRecord(80.0,  "DiastolicPressure", 1000L);
        patient.addRecord(97.0,  "Saturation",        1000L);

        assertEquals(3, patient.getRecords(0L, Long.MAX_VALUE).size());
    }

    @Test
    void testRecordStoredWithCorrectPatientId() {
        patient.addRecord(98.6, "Temperature", 5000L);
        PatientRecord r = patient.getRecords(0L, Long.MAX_VALUE).get(0);
        assertEquals(42, r.getPatientId());
        assertEquals("Temperature", r.getRecordType());
        assertEquals(98.6, r.getMeasurementValue());
        assertEquals(5000L, r.getTimestamp());
    }
}
