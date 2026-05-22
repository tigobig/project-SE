package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.ECGAlert;

/**
 * Factory that creates ECGAlert instances.
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates a new ECGAlert.
     *
     * @param patientId the identifier of the patient for whom the alert is created
     * @param condition a short description of the ECG condition detected
     * @param timestamp the time of detection, in milliseconds since the Unix epoch
     * @return a new ECGAlert instance
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}
