package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alert strategy that checks for blood pressure threshold violations and consistent trends.
 *
 * Critical thresholds: systolic above 180 or below 90 mmHg; diastolic above 120 or below 60 mmHg.
 * Trend condition: three consecutive readings each changing by more than 10 mmHg in the same direction.
 */
public class BloodPressureStrategy implements AlertStrategy {

    private final BloodPressureAlertFactory factory = new BloodPressureAlertFactory();

    /**
     * Evaluates systolic and diastolic pressure records for the patient and returns any
     * threshold or trend alerts.
     *
     * @param patient the patient to evaluate; must not be null
     * @return a list of blood pressure alerts; never null, may be empty
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();
        checkPressureForLabel(patient, "SystolicPressure",  180.0, 90.0,  alerts);
        checkPressureForLabel(patient, "DiastolicPressure", 120.0, 60.0,  alerts);
        return alerts;
    }

    private void checkPressureForLabel(Patient patient, String label,
                                       double highThreshold, double lowThreshold,
                                       List<Alert> alerts) {
        List<PatientRecord> records = getRecords(patient, label);
        if (records.isEmpty()) return;

        PatientRecord latest = records.get(records.size() - 1);
        long ts = latest.getTimestamp();
        String id = String.valueOf(patient.getPatientId());

        if (latest.getMeasurementValue() > highThreshold) {
            alerts.add(factory.createAlert(id, "Critical High " + label, ts));
        }
        if (latest.getMeasurementValue() < lowThreshold) {
            alerts.add(factory.createAlert(id, "Critical Low " + label, ts));
        }

        if (records.size() >= 3) {
            double v1 = records.get(records.size() - 3).getMeasurementValue();
            double v2 = records.get(records.size() - 2).getMeasurementValue();
            double v3 = records.get(records.size() - 1).getMeasurementValue();

            if (v2 - v1 > 10 && v3 - v2 > 10) {
                alerts.add(factory.createAlert(id, "Increasing " + label + " Trend", ts));
            }
            if (v1 - v2 > 10 && v2 - v3 > 10) {
                alerts.add(factory.createAlert(id, "Decreasing " + label + " Trend", ts));
            }
        }
    }

    private List<PatientRecord> getRecords(Patient patient, String type) {
        return patient.getRecords(0L, Long.MAX_VALUE).stream()
            .filter(r -> type.equals(r.getRecordType()))
            .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
            .collect(Collectors.toList());
    }
}
