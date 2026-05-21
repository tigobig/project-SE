package com.cardio_generator.outputs;

/**
 * Defines the contract for all output strategies used by the health data simulation.
 *
 * An OutputStrategy receives individual patient data records and is responsible
 * for delivering them to a specific destination such as the console, a file, a WebSocket
 * connection, or a TCP socket. Concrete implementations are free to buffer, format or
 * transmit data in any way appropriate for their target medium.
 *
 * All implementations must be safe to call concurrently from multiple threads, as the
 * simulator schedules generators for many patients in parallel.
 */
public interface OutputStrategy {

    /**
     * Delivers a single patient health data record to the output destination.
     *
     * @param patientId the unique identifier of the patient whose data is being reported;
     *                  must be a positive integer
     * @param timestamp the time at which the measurement was taken, expressed as
     *                  milliseconds 
     * @param label     a short string identifying the type of health metric
     *                  
     * @param data      the measured value formatted as a string
     *                  
     */
    void output(int patientId, long timestamp, String label, String data);
}
