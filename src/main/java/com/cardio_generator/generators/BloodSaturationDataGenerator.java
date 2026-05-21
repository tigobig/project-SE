package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated blood oxygen saturation data for patients.
 *
 * Each patient's saturation value is initialised to a random baseline in the range
 * 95–100% and evolves over time through small random fluctuations (+-1% per step). The
 * value is clamped to the physiologically realistic range of 90–100% to prevent the
 * simulation from producing impossible readings.
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Constructs a generator for the given number of patients and initialises each patient's
     * baseline saturation to a random value between 95% and 100% (inclusive).
   
     * @param patientCount the total number of patients to track, must be non-negative
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Simulates one time-step of blood saturation for the specified patient and emits the result.
     *
     * A random variation of -1, 0, or +1 is applied to the patient's last recorded value.
     * The result is clamped to [90, 100] before being stored and output. The output data string
     * is formatted as an integer percentage
     *
     * @param patientId      the unique identifier of the patient
     * @param outputStrategy the strategy that will receive the generated saturation record
     * @throws RuntimeException if an unexpected error occurs during value generation or output
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
