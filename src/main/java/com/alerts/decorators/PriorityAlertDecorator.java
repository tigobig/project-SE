package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that prepends a priority level to an alert's condition string.
 * For example, wrapping an alert with condition "Low Blood Saturation" and
 * priority level "HIGH" produces the condition "HIGH Low Blood Saturation".
 */
public class PriorityAlertDecorator extends AlertDecorator {

    private final String priorityLevel;

    /**
     * Constructs a PriorityAlertDecorator.
     *
     * @param alert         the Alert to wrap; must not be null
     * @param priorityLevel the priority label to prepend, for example "HIGH" or "MEDIUM"
     */
    public PriorityAlertDecorator(Alert alert, String priorityLevel) {
        super(alert);
        this.priorityLevel = priorityLevel;
    }

    /**
     * Returns the priority level assigned to this alert.
     *
     * @return the priority level string
     */
    public String getPriorityLevel() {
        return priorityLevel;
    }

    /**
     * Returns the condition with the priority tag prepended.
     * The format is "(priorityLevel) originalCondition".
     *
     * @return the augmented condition string
     */
    @Override
    public String getCondition() {
        return "(" + priorityLevel + ") " + decoratedAlert.getCondition();
    }
}
