package com.alerts;

/**
 * Represents an alert related to an abnormal ECG reading.
 * This specialisation of Alert carries no additional state beyond what
 * the base class already provides.
 */
public class ECGAlert extends Alert {

    /**
     * Constructs an ECGAlert with the given patient identifier, condition
     * description, and timestamp.
     *
     * @param patientId the identifier of the patient for whom the alert is raised
     * @param condition a short description of the ECG condition detected
     * @param timestamp the time at which the condition was detected, in milliseconds since the Unix epoch
     */
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
