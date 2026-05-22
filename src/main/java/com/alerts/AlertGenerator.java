package com.alerts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * Analyses patient records stored in DataStorage and fires alerts when predefined
 * clinical conditions are met.
 *
 * Alert evaluation is delegated to a fixed list of AlertStrategy implementations:
 * BloodPressureStrategy, OxygenSaturationStrategy, and HeartRateStrategy.
 * In addition, the patient-triggered alert button is checked directly.
 *
 * All triggered alerts are stored in an internal list accessible via
 * getTriggeredAlerts() for inspection and testing.
 */
public class AlertGenerator {

    private final List<Alert> triggeredAlerts = new ArrayList<>();
    private final List<AlertStrategy> strategies;

    /**
     * Constructs an AlertGenerator backed by the given DataStorage.
     *
     * @param dataStorage the storage from which patient records are retrieved; must not be null
     */
    public AlertGenerator(DataStorage dataStorage) {
        strategies = new ArrayList<>();
        strategies.add(new BloodPressureStrategy());
        strategies.add(new OxygenSaturationStrategy());
        strategies.add(new HeartRateStrategy());
    }

    /**
     * Evaluates all defined alert conditions for the given patient and fires an alert
     * for every condition that is met.
     *
     * @param patient the patient whose records are to be analysed; must not be null
     */
    public void evaluateData(Patient patient) {
        for (AlertStrategy strategy : strategies) {
            for (Alert alert : strategy.checkAlert(patient)) {
                triggerAlert(alert);
            }
        }
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

    /**
     * Fires and records an alert.
     *
     * @param alert the alert to fire; must not be null
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
        System.err.printf("ALERT - Patient %s: %s at %d%n",
            alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
    }

    /**
     * Returns all records of the given type for a patient, sorted by timestamp ascending.
     *
     * @param patient    the patient whose records to retrieve
     * @param recordType the label to filter on
     * @return sorted, filtered list of records; never null
     */
    private List<PatientRecord> getRecordsByType(Patient patient, String recordType) {
        return patient.getRecords(0L, Long.MAX_VALUE).stream()
            .filter(r -> recordType.equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    }
}
