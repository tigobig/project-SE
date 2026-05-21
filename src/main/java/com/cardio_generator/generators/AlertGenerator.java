package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated alert events for patients in the cardiovascular health monitoring system.
 *
 * Each patient tracks a boolean alert state (active or resolved). On every call to
 * , an active alert has a 90% probability of being resolved, while a patient
 * with no active alert may have a new one triggered based on a Poisson-process probability model.
 
 */
public class AlertGenerator implements PatientDataGenerator {

    // Fixed constant name from randomGenerator to RANDOM_GENERATOR
    // (static final fields) must use UPPER_SNAKE_CASE 
    public static final Random RANDOM_GENERATOR = new Random();

    // Fixed field name from AlertStates to alertStates non-constant field names must use loweCamelcase
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs an AlertGenerator for the given number of patients.
     * @param patientCount the total number of patients whose alert states will be tracked;
     *                     must be non-negative
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Simulates one time-step of alert activity for the specified patient and emits the result.
     * @param patientId      the unique identifier of the patient (1-based index)
     * @param outputStrategy the strategy used to emit the alert event; must not be null
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Fixed local variable name from Lambda to lambda local variable names must use lowerCamelcase
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
