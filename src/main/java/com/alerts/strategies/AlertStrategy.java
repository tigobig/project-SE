package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import java.util.List;

/**
 * Defines a single alert-checking strategy.
 * Each implementation encapsulates the logic for one category of clinical alert.
 */
public interface AlertStrategy {

    /**
     * Evaluates the given patient's records and returns any alerts that should be fired.
     *
     * @param patient the patient whose records are to be evaluated; must not be null
     * @return a list of Alert objects representing every condition detected; never null, may be empty
     */
    List<Alert> checkAlert(Patient patient);
}
