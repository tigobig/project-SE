package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BloodPressureAlert;

/**
 * Factory that creates BloodPressureAlert instances.
 */
public class BloodPressureAlertFactory extends AlertFactory {

    /**
     * Creates a new BloodPressureAlert.
     *
     * @param patientId the identifier of the patient for whom the alert is created
     * @param condition a short description of the blood pressure condition detected
     * @param timestamp the time of detection, in milliseconds since the Unix epoch
     * @return a new BloodPressureAlert instance
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}
