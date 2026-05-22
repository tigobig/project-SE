package com.alerts;

/**
 * Represents an alert related to abnormal blood oxygen saturation readings.
 * This specialisation of Alert carries no additional state beyond what
 * the base class already provides.
 */
public class BloodOxygenAlert extends Alert {

    /**
     * Constructs a BloodOxygenAlert with the given patient identifier, condition
     * description, and timestamp.
     *
     * @param patientId the identifier of the patient for whom the alert is raised
     * @param condition a short description of the blood oxygen condition detected
     * @param timestamp the time at which the condition was detected, in milliseconds since the Unix epoch
     */
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }
}
