package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Base decorator for Alert objects.
 * Wraps an existing Alert and delegates all accessor calls to it.
 * Subclasses may override specific methods to augment the wrapped alert's behaviour.
 */
public class AlertDecorator extends Alert {

    /** The alert instance being decorated. */
    protected final Alert decoratedAlert;

    /**
     * Constructs an AlertDecorator that wraps the given alert.
     *
     * @param alert the Alert to wrap; must not be null
     */
    public AlertDecorator(Alert alert) {
        super(alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
        this.decoratedAlert = alert;
    }

    /**
     * Returns the condition of the wrapped alert.
     *
     * @return the condition string from the decorated Alert
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition();
    }
}
