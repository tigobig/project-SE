package com.alerts.factories;

import com.alerts.Alert;

/**
 * Abstract factory for creating Alert objects.
 * Subclasses determine which concrete Alert type is instantiated.
 */
public abstract class AlertFactory {

    /**
     * Creates a new Alert of the type determined by the concrete factory.
     *
     * @param patientId the identifier of the patient for whom the alert is created
     * @param condition a short description of the clinical condition detected
     * @param timestamp the time at which the condition was detected, in milliseconds since the Unix epoch
     * @return a new Alert instance
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
