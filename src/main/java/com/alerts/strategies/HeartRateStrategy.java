package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alert strategy that detects abnormal ECG peaks using a sliding-window statistical approach.
 *
 * The strategy examines the last 20 ECG readings. An alert is fired when the most recent
 * reading deviates by more than 2 standard deviations from the mean of those readings.
 */
public class HeartRateStrategy implements AlertStrategy {

    private final ECGAlertFactory factory = new ECGAlertFactory();

    /**
     * Evaluates ECG records for the patient and returns an abnormal-peak alert when warranted.
     *
     * @param patient the patient to evaluate; must not be null
     * @return a list containing at most one ECG alert; never null, may be empty
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> records = getRecords(patient, "ECG");
        if (records.size() < 2) return alerts;

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
            alerts.add(factory.createAlert(
                String.valueOf(patient.getPatientId()),
                "Abnormal ECG Peak",
                latest.getTimestamp()));
        }

        return alerts;
    }

    private List<PatientRecord> getRecords(Patient patient, String type) {
        return patient.getRecords(0L, Long.MAX_VALUE).stream()
            .filter(r -> type.equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    }
}
