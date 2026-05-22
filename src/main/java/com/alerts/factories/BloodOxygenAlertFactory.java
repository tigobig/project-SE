package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BloodOxygenAlert;

/**
 * Factory that creates BloodOxygenAlert instances.
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    /**
     * Creates a new BloodOxygenAlert.
     *
     * @param patientId the identifier of the patient for whom the alert is created
     * @param condition a short description of the blood oxygen condition detected
     * @param timestamp the time of detection, in milliseconds since the Unix epoch
     * @return a new BloodOxygenAlert instance
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}
