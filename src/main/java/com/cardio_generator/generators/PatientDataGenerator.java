package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the contract for all patient health-data generators in the simulation.
 * Generators are expected to be called periodically by a scheduler and must be
 * thread-safe if invoked concurrently for different patient IDs.
 */
public interface PatientDataGenerator {

    /**
     * Generates one data sample for the specified patient and forwards it to the given
     * output strategy.
     * @param patientId      the unique identifier of the patient for whom data should be
     *                       generated, must be a valid index in the generator's internal state
     * @param outputStrategy the strategy that will receive and process the generated data
     *                       point
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
