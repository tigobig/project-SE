package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that appends a repeat count to an alert's condition string.
 * For example, wrapping an alert with condition "Abnormal ECG Peak" and
 * repeat count 3 produces "Abnormal ECG Peak (repeated 3 times)".
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;

    /**
     * Constructs a RepeatedAlertDecorator.
     *
     * @param alert       the Alert to wrap; must not be null
     * @param repeatCount the number of times the alert has been repeated
     */
    public RepeatedAlertDecorator(Alert alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    /**
     * Returns the number of times the alert has been repeated.
     *
     * @return the repeat count
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Returns the condition with the repeat count appended.
     * The format is "originalCondition (repeated N times)".
     *
     * @return the augmented condition string
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " (repeated " + repeatCount + " times)";
    }
}
