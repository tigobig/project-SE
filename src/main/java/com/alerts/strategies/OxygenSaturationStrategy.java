package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Alert strategy that checks blood oxygen saturation levels.
 *
 * Three conditions are evaluated:
 * Low saturation: the most recent reading is below 92 percent.
 * Rapid drop: saturation falls 5 percent or more within any 10-minute window ending at the latest reading.
 * Hypotensive hypoxemia: the most recent systolic pressure is below 90 mmHg and saturation is below 92 percent simultaneously.
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    private final BloodOxygenAlertFactory factory = new BloodOxygenAlertFactory();

    /**
     * Evaluates saturation and systolic records for the patient and returns any
     * oxygen-related alerts.
     *
     * @param patient the patient to evaluate must not be null
     * @return a list of oxygen saturation alerts never null, may be empty
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> satRecords = getRecords(patient, "Saturation");
        if (satRecords.isEmpty()) return alerts;

        PatientRecord latest = satRecords.get(satRecords.size() - 1);
        String id = String.valueOf(patient.getPatientId());

        if (latest.getMeasurementValue() < 92.0) {
            alerts.add(factory.createAlert(id, "Low Blood Saturation", latest.getTimestamp()));
        }

        long tenMinutesAgo = latest.getTimestamp() - 600_000L;
        List<PatientRecord> recent = satRecords.stream()
            .filter(r -> r.getTimestamp() >= tenMinutesAgo)
            .collect(Collectors.toList());

        if (recent.size() >= 2) {
            double maxRecent = recent.stream()
                .mapToDouble(PatientRecord::getMeasurementValue)
                .max().getAsDouble();
            if (maxRecent - latest.getMeasurementValue() >= 5.0) {
                alerts.add(factory.createAlert(id, "Rapid Blood Saturation Drop", latest.getTimestamp()));
            }
        }

        List<PatientRecord> systolicRecords = getRecords(patient, "SystolicPressure");
        if (!systolicRecords.isEmpty()) {
            double latestSystolic = systolicRecords.get(systolicRecords.size() - 1).getMeasurementValue();
            if (latestSystolic < 90.0 && latest.getMeasurementValue() < 92.0) {
                long ts = Math.max(
                    systolicRecords.get(systolicRecords.size() - 1).getTimestamp(),
                    latest.getTimestamp());
                alerts.add(factory.createAlert(id, "Hypotensive Hypoxemia", ts));
            }
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
