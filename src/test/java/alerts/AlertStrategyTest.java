package alerts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

class AlertStrategyTest {

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    private Patient patient(int id) {
        for (Patient p : storage.getAllPatients()) {
            if (p.getPatientId() == id) return p;
        }
        return null;
    }

    private boolean hasCondition(List<Alert> alerts, String condition) {
        return alerts.stream().anyMatch(a -> condition.equals(a.getCondition()));
    }

    // BloodPressureStrategy

    @Test
    void testCriticalHighSystolicFires() {
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(patient(1));
        assertTrue(hasCondition(alerts, "Critical High SystolicPressure"));
    }

    @Test
    void testCriticalLowSystolicFires() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(patient(1));
        assertTrue(hasCondition(alerts, "Critical Low SystolicPressure"));
    }

    @Test
    void testIncreasingSystolicTrendFires() {
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 140.0, "SystolicPressure", 3000L);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(patient(1));
        assertTrue(hasCondition(alerts, "Increasing SystolicPressure Trend"));
    }

    @Test
    void testDecreasingSystolicTrendFires() {
        storage.addPatientData(1, 140.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 110.0, "SystolicPressure", 3000L);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(patient(1));
        assertTrue(hasCondition(alerts, "Decreasing SystolicPressure Trend"));
    }

    @Test
    void testNormalSystolicReturnsEmpty() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        List<Alert> alerts = new BloodPressureStrategy().checkAlert(patient(1));
        assertFalse(hasCondition(alerts, "Critical High SystolicPressure"));
        assertFalse(hasCondition(alerts, "Critical Low SystolicPressure"));
        assertFalse(hasCondition(alerts, "Increasing SystolicPressure Trend"));
        assertFalse(hasCondition(alerts, "Decreasing SystolicPressure Trend"));
    }

    // OxygenSaturationStrategy

    @Test
    void testLowSaturationFires() {
        storage.addPatientData(2, 90.0, "Saturation", 1000L);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(patient(2));
        assertTrue(hasCondition(alerts, "Low Blood Saturation"));
    }

    @Test
    void testRapidSaturationDropFires() {
        long now = System.currentTimeMillis();
        storage.addPatientData(2, 98.0, "Saturation", now - 300_000L);
        storage.addPatientData(2, 92.0, "Saturation", now);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(patient(2));
        assertTrue(hasCondition(alerts, "Rapid Blood Saturation Drop"));
    }

    @Test
    void testHypotensiveHypoxemiaFires() {
        storage.addPatientData(2, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(2, 88.0, "Saturation", 1000L);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(patient(2));
        assertTrue(hasCondition(alerts, "Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaDoesNotFireWhenOnlyBPLow() {
        storage.addPatientData(2, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(2, 95.0, "Saturation", 1000L);
        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(patient(2));
        assertFalse(hasCondition(alerts, "Hypotensive Hypoxemia"));
    }

    // HeartRateStrategy

    @Test
    void testAbnormalECGPeakFires() {
        for (int i = 0; i < 19; i++) {
            storage.addPatientData(3, 0.1, "ECG", 1000L + i * 100L);
        }
        storage.addPatientData(3, 5.0, "ECG", 3000L);
        List<Alert> alerts = new HeartRateStrategy().checkAlert(patient(3));
        assertTrue(hasCondition(alerts, "Abnormal ECG Peak"));
    }

    @Test
    void testHeartRateStrategyReturnsEmptyForFewerThan2Records() {
        storage.addPatientData(3, 0.5, "ECG", 1000L);
        List<Alert> alerts = new HeartRateStrategy().checkAlert(patient(3));
        assertTrue(alerts.isEmpty());
    }
}
