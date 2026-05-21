package com.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Analyses patient records stored in DataStorage and fires alerts when predefined
 * clinical conditions are met.
 *
 * Alert types evaluated for each patient:
 *   - Blood pressure critical threshold (systolic above 180 or below 90; diastolic above 120 or below 60)
 *   - Blood pressure trend (three consecutive readings each changing by more than 10 mmHg)
 *   - Low blood saturation (below 92 percent)
 *   - Rapid saturation drop (5 percent or more within any 10-minute window)
 *   - Hypotensive hypoxemia (systolic below 90 and saturation below 92 percent)
 *   - Abnormal ECG peak (more than 2 standard deviations above the sliding-window mean)
 *   - Patient/nurse triggered alert button
 *
 * All triggered alerts are stored in an internal list accessible via
 * getTriggeredAlerts() for inspection and testing.
 */
public class AlertGenerator {

    private final DataStorage dataStorage;
    private final List<Alert> triggeredAlerts = new ArrayList<>();

    /**
     * Constructs an AlertGenerator backed by the given DataStorage.
     *
     * @param dataStorage the storage from which patient records are retrieved; must not be null
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates all defined alert conditions for the given patient and fires an alert
     * for every condition that is met.
     *
     * @param patient the patient whose records are to be analysed; must not be null
     */
    public void evaluateData(Patient patient) {
        checkBloodPressureAlerts(patient);
        checkBloodSaturationAlerts(patient);
        checkHypotensiveHypoxemia(patient);
        checkECGAlerts(patient);
        checkTriggeredAlert(patient);
    }

    /**
     * Returns an unmodifiable view of all alerts that have been triggered so far.
     *
     * @return unmodifiable list of triggered Alert objects
     */
    public List<Alert> getTriggeredAlerts() {
        return Collections.unmodifiableList(triggeredAlerts);
    }

    // -------------------------------------------------------------------------
    // Blood Pressure
    // -------------------------------------------------------------------------

    /**
     * Checks both systolic and diastolic blood pressure for critical threshold violations
     * and for consistent three-reading trends.
     *
     * @param patient the patient to evaluate
     */
    private void checkBloodPressureAlerts(Patient patient) {
        checkPressureForLabel(patient, "SystolicPressure",  180.0, 90.0);
        checkPressureForLabel(patient, "DiastolicPressure", 120.0, 60.0);
    }

    /**
     * Evaluates critical thresholds and trend conditions for a single pressure label.
     *
     * @param patient        the patient to evaluate
     * @param label          "SystolicPressure" or "DiastolicPressure"
     * @param highThreshold  value above which a critical-high alert is fired
     * @param lowThreshold   value below which a critical-low alert is fired
     */
    private void checkPressureForLabel(Patient patient, String label,
                                       double highThreshold, double lowThreshold) {
        List<PatientRecord> records = getRecordsByType(patient, label);
        if (records.isEmpty()) return;

        PatientRecord latest = records.get(records.size() - 1);
        long ts = latest.getTimestamp();
        String id = String.valueOf(patient.getPatientId());

        // Critical threshold
        if (latest.getMeasurementValue() > highThreshold) {
            triggerAlert(new Alert(id, "Critical High " + label, ts));
        }
        if (latest.getMeasurementValue() < lowThreshold) {
            triggerAlert(new Alert(id, "Critical Low " + label, ts));
        }

        // Trend: three consecutive readings each changing by more than 10 mmHg
        if (records.size() >= 3) {
            double v1 = records.get(records.size() - 3).getMeasurementValue();
            double v2 = records.get(records.size() - 2).getMeasurementValue();
            double v3 = records.get(records.size() - 1).getMeasurementValue();

            if (v2 - v1 > 10 && v3 - v2 > 10) {
                triggerAlert(new Alert(id, "Increasing " + label + " Trend", ts));
            }
            if (v1 - v2 > 10 && v2 - v3 > 10) {
                triggerAlert(new Alert(id, "Decreasing " + label + " Trend", ts));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Blood Saturation
    // -------------------------------------------------------------------------

    /**
     * Checks for low saturation (below 92 percent) and for a rapid drop of 5 percent or more within any
     * 10-minute window ending at the most recent reading.
     *
     * @param patient the patient to evaluate
     */
    private void checkBloodSaturationAlerts(Patient patient) {
        List<PatientRecord> records = getRecordsByType(patient, "Saturation");
        if (records.isEmpty()) return;

        PatientRecord latest = records.get(records.size() - 1);
        String id = String.valueOf(patient.getPatientId());

        // Low saturation alert
        if (latest.getMeasurementValue() < 92.0) {
            triggerAlert(new Alert(id, "Low Blood Saturation", latest.getTimestamp()));
        }

        // Rapid drop: 5 percent or more within the last 10 minutes
        long tenMinutesAgo = latest.getTimestamp() - 600_000L;
        List<PatientRecord> recentRecords = records.stream()
            .filter(r -> r.getTimestamp() >= tenMinutesAgo)
            .collect(Collectors.toList());

        if (recentRecords.size() >= 2) {
            double maxRecent = recentRecords.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .max().getAsDouble();
            if (maxRecent - latest.getMeasurementValue() >= 5.0) {
                triggerAlert(new Alert(id, "Rapid Blood Saturation Drop", latest.getTimestamp()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hypotensive Hypoxemia (combined alert)
    // -------------------------------------------------------------------------

    /**
     * Fires a hypotensive hypoxemia alert when the most recent systolic blood pressure
     * is below 90 mmHg and the most recent blood saturation is below 92% simultaneously.
     *
     * @param patient the patient to evaluate
     */
    private void checkHypotensiveHypoxemia(Patient patient) {
        List<PatientRecord> systolicRecords = getRecordsByType(patient, "SystolicPressure");
        List<PatientRecord> satRecords      = getRecordsByType(patient, "Saturation");
        if (systolicRecords.isEmpty() || satRecords.isEmpty()) return;

        double latestSystolic = systolicRecords.get(systolicRecords.size() - 1).getMeasurementValue();
        double latestSat      = satRecords.get(satRecords.size() - 1).getMeasurementValue();

        if (latestSystolic < 90.0 && latestSat < 92.0) {
            long ts = Math.max(
                systolicRecords.get(systolicRecords.size() - 1).getTimestamp(),
                satRecords.get(satRecords.size() - 1).getTimestamp());
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia", ts));
        }
    }

    // -------------------------------------------------------------------------
    // ECG
    // -------------------------------------------------------------------------

    /**
     * Detects abnormal ECG peaks by comparing the most recent reading against a sliding
     * window of the preceding 20 readings. An alert is fired when the latest value
     * deviates by more than 2 standard deviations from the window mean.
     *
     * @param patient the patient to evaluate
     */
    private void checkECGAlerts(Patient patient) {
        List<PatientRecord> records = getRecordsByType(patient, "ECG");
        if (records.size() < 2) return;

        int windowSize = Math.min(records.size(), 20);
        List<PatientRecord> window = records.subList(records.size() - windowSize, records.size());

        double mean = window.stream()
            .mapToDouble(PatientRecord::getMeasurementValue)
            .average()
            .getAsDouble();

        double variance = window.stream()
            .mapToDouble(r -> Math.pow(r.getMeasurementValue() - mean, 2))
            .average()
            .getAsDouble();
        double stdDev = Math.sqrt(variance);

        PatientRecord latest = records.get(records.size() - 1);
        if (stdDev > 0 && Math.abs(latest.getMeasurementValue() - mean) > 2.0 * stdDev) {
            triggerAlert(new Alert(
                String.valueOf(patient.getPatientId()),
                "Abnormal ECG Peak",
                latest.getTimestamp()));
        }
    }

    // -------------------------------------------------------------------------
    // Triggered / manual alert
    // -------------------------------------------------------------------------

    /**
     * Checks whether the most recent "Alert" record has value 1.0 (mapped from "triggered")
     * and fires a patient-triggered alert if so.
     *
     * @param patient the patient to evaluate
     */
    private void checkTriggeredAlert(Patient patient) {
        List<PatientRecord> records = getRecordsByType(patient, "Alert");
        if (records.isEmpty()) return;

        PatientRecord latest = records.get(records.size() - 1);
        if (latest.getMeasurementValue() == 1.0) {
            triggerAlert(new Alert(
                String.valueOf(patient.getPatientId()),
                "Patient Triggered Alert",
                latest.getTimestamp()));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Fires and records an alert. Writes a message to stderr so that alerts are visible
     * in the console during a live run.
     *
     * @param alert the alert to fire; must not be null
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
        System.err.printf("ALERT – Patient %s: %s at %d%n",
            alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
    }

    /**
     * Returns all records of the given type for a patient, sorted by timestamp ascending.
     *
     * @param patient    the patient whose records to retrieve
     * @param recordType the label to filter on (e.g. "SystolicPressure")
     * @return sorted, filtered list of records; never null
     */
    private List<PatientRecord> getRecordsByType(Patient patient, String recordType) {
        return patient.getRecords(0L, Long.MAX_VALUE).stream()
            .filter(r -> recordType.equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    }
}
