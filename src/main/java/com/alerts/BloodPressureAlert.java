package com.alerts;

/**
 * Represents an alert related to abnormal blood pressure readings.
 * This specialisation of Alert carries no additional state beyond what
 * the base class already provides.
 */
public class BloodPressureAlert extends Alert {

    /**
     * Constructs a BloodPressureAlert with the given patient identifier, condition
     * description, and timestamp.
     *
     * @param patientId the identifier of the patient for whom the alert is raised
     * @param condition a short description of the blood pressure condition detected
     * @param timestamp the time at which the condition was detected, in milliseconds since the Unix epoch
     */
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
