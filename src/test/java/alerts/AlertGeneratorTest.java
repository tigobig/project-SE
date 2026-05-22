package alerts;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

/**
 * Tests for AlertGenerator covering all five alert categories:
 * - Blood pressure critical thresholds
 * - Blood pressure trends
 * - Blood saturation (low + rapid drop)
 * - Hypotensive hypoxemia (combined)
 * - ECG abnormal peak
 * - Patient-triggered alert
 */
class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        alertGenerator = new AlertGenerator(storage);
    }

    //=========================================================================
    //Blood Pressure — Critical Thresholds
    //=========================================================================

    @Test
    void testCriticalHighSystolicPressureFiresAlert() {
        storage.addPatientData(1, 185.0, "SystolicPressure", 1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Critical High SystolicPressure"));
    }

    @Test
    void testCriticalLowSystolicPressureFiresAlert() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Critical Low SystolicPressure"));
    }

    @Test
    void testCriticalHighDiastolicPressureFiresAlert() {
        storage.addPatientData(1, 125.0, "DiastolicPressure", 1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Critical High DiastolicPressure"));
    }

    @Test
    void testCriticalLowDiastolicPressureFiresAlert() {
        storage.addPatientData(1, 55.0, "DiastolicPressure", 1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Critical Low DiastolicPressure"));
    }

    @Test
    void testNormalBloodPressureDoesNotFireCriticalAlert() {
        storage.addPatientData(1, 120.0, "SystolicPressure",  1000L);
        storage.addPatientData(1, 80.0,  "DiastolicPressure", 1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Critical High SystolicPressure"));
        assertFalse(containsCondition("Critical Low SystolicPressure"));
        assertFalse(containsCondition("Critical High DiastolicPressure"));
        assertFalse(containsCondition("Critical Low DiastolicPressure"));
    }

    //=========================================================================
    //Blood Pressure — Trends
    //=========================================================================

    @Test
    void testIncreasingBloodPressureTrendFiresAlert() {
        // Each step > 10 mmHg increase
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 140.0, "SystolicPressure", 3000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Increasing SystolicPressure Trend"));
    }

    @Test
    void testDecreasingBloodPressureTrendFiresAlert() {
        // Each step > 10 mmHg decrease
        storage.addPatientData(1, 140.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 110.0, "SystolicPressure", 3000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Decreasing SystolicPressure Trend"));
    }

    @Test
    void testSmallIncreasesDoNotTriggerTrendAlert() {
        // Steps of 2 mmHg — below the 10 mmHg threshold
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 122.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 124.0, "SystolicPressure", 3000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Increasing SystolicPressure Trend"));
    }

    @Test
    void testMixedDirectionDoesNotTriggerTrendAlert() {
        // Up then down — not a consistent trend
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125.0, "SystolicPressure", 2000L);
        storage.addPatientData(1, 112.0, "SystolicPressure", 3000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Increasing SystolicPressure Trend"));
        assertFalse(containsCondition("Decreasing SystolicPressure Trend"));
    }

    @Test
    void testDiastolicIncreasingTrendFiresAlert() {
        storage.addPatientData(1, 65.0, "DiastolicPressure", 1000L);
        storage.addPatientData(1, 80.0, "DiastolicPressure", 2000L);
        storage.addPatientData(1, 95.0, "DiastolicPressure", 3000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Increasing DiastolicPressure Trend"));
    }

    //=========================================================================
    //Blood Saturation
    //=========================================================================

    @Test
    void testLowSaturationBelowThresholdFiresAlert() {
        storage.addPatientData(1, 90.0, "Saturation", 1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Low Blood Saturation"));
    }

    @Test
    void testSaturationAt92DoesNotFireLowAlert() {
        // Boundary: exactly 92% should NOT trigger (condition is < 92)
        storage.addPatientData(1, 92.0, "Saturation", 1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Low Blood Saturation"));
    }

    @Test
    void testNormalSaturationDoesNotFireAlert() {
        storage.addPatientData(1, 98.0, "Saturation", 1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Low Blood Saturation"));
    }

    @Test
    void testRapidSaturationDropWithin10MinutesFiresAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 98.0, "Saturation", now - 300_000L); // 5 min ago
        storage.addPatientData(1, 92.0, "Saturation", now);            // now, drop of 6%
        evaluatePatient(1);
        assertTrue(containsCondition("Rapid Blood Saturation Drop"));
    }

    @Test
    void testRapidDropOlderThan10MinutesDoesNotFireAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 98.0, "Saturation", now - 700_000L); // 11+ min ago
        storage.addPatientData(1, 92.0, "Saturation", now);
        evaluatePatient(1);
        assertFalse(containsCondition("Rapid Blood Saturation Drop"));
    }

    @Test
    void testDropOfExactly5PercentFiresRapidDropAlert() {
        long now = System.currentTimeMillis();
        storage.addPatientData(1, 97.0, "Saturation", now - 60_000L); // 1 min ago
        storage.addPatientData(1, 92.0, "Saturation", now);           // drop of exactly 5%
        evaluatePatient(1);
        assertTrue(containsCondition("Rapid Blood Saturation Drop"));
    }

    //=========================================================================
    //Hypotensive Hypoxemia (combined)
    //=========================================================================

    @Test
    void testHypotensiveHypoxemiaWhenBothConditionsMet() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 88.0, "Saturation",       1000L);
        evaluatePatient(1);
        assertTrue(containsCondition("Hypotensive Hypoxemia"));
    }

    @Test
    void testNoHypotensiveHypoxemiaWhenOnlyLowBP() {
        storage.addPatientData(1, 85.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 95.0, "Saturation",       1000L); // saturation is fine
        evaluatePatient(1);
        assertFalse(containsCondition("Hypotensive Hypoxemia"));
    }

    @Test
    void testNoHypotensiveHypoxemiaWhenOnlyLowSaturation() {
        storage.addPatientData(1, 110.0, "SystolicPressure", 1000L); // BP is fine
        storage.addPatientData(1, 88.0,  "Saturation",       1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Hypotensive Hypoxemia"));
    }

    @Test
    void testNoHypotensiveHypoxemiaWhenBothNormal() {
        storage.addPatientData(1, 120.0, "SystolicPressure", 1000L);
        storage.addPatientData(1, 97.0,  "Saturation",       1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Hypotensive Hypoxemia"));
    }

    //=========================================================================
    //ECG
    //=========================================================================

    @Test
    void testAbnormalECGPeakFiresAlert() {
        // 19 readings near 0.1, then a clear outlier at 5.0
        for (int i = 0; i < 19; i++) {
            storage.addPatientData(1, 0.1, "ECG", 1000L + i * 100L);
        }
        storage.addPatientData(1, 5.0, "ECG", 3000L); // outlier > 2 std devs
        evaluatePatient(1);
        assertTrue(containsCondition("Abnormal ECG Peak"));
    }

    @Test
    void testNormalECGValuesDoNotFireAlert() {
        // 20 readings that increase very slightly — no statistical outlier
        for (int i = 0; i < 20; i++) {
            storage.addPatientData(1, 0.5 + i * 0.01, "ECG", 1000L + i * 100L);
        }
        evaluatePatient(1);
        assertFalse(containsCondition("Abnormal ECG Peak"));
    }

    @Test
    void testECGWithFewerThan2RecordsDoesNotFireAlert() {
        storage.addPatientData(1, 0.8, "ECG", 1000L);
        evaluatePatient(1);
        assertFalse(containsCondition("Abnormal ECG Peak"));
    }

    //=========================================================================
    //Triggered alert (patient/nurse button)
    //=========================================================================

    @Test
    void testPatientTriggeredAlertFiresWhenValueIs1() {
        storage.addPatientData(1, 1.0, "Alert", 1000L); // 1.0 = "triggered"
        evaluatePatient(1);
        assertTrue(containsCondition("Patient Triggered Alert"));
    }

    @Test
    void testResolvedAlertDoesNotFireTriggeredAlert() {
        storage.addPatientData(1, 0.0, "Alert", 1000L); // 0.0 = "resolved"
        evaluatePatient(1);
        assertFalse(containsCondition("Patient Triggered Alert"));
    }

    @Test
    void testAlertObjectContainsCorrectPatientId() {
        storage.addPatientData(7, 1.0, "Alert", 9999L);
        evaluatePatient(7);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertTrue(alerts.stream().anyMatch(a -> "7".equals(a.getPatientId())));
    }

    @Test
    void testAlertObjectContainsCorrectTimestamp() {
        storage.addPatientData(1, 1.0, "Alert", 12345L);
        evaluatePatient(1);
        List<Alert> alerts = alertGenerator.getTriggeredAlerts();
        assertTrue(alerts.stream()
            .filter(a -> "Patient Triggered Alert".equals(a.getCondition()))
            .anyMatch(a -> a.getTimestamp() == 12345L));
    }

    //=========================================================================
    //Helpers
    //=========================================================================

    private void evaluatePatient(int patientId) {
        for (Patient p : storage.getAllPatients()) {
            if (p.getPatientId() == patientId) {
                alertGenerator.evaluateData(p);
                return;
            }
        }
    }

    private boolean containsCondition(String condition) {
        return alertGenerator.getTriggeredAlerts().stream()
            .anyMatch(a -> condition.equals(a.getCondition()));
    }
}
