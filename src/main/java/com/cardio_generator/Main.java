package com.cardio_generator;

import com.data_management.DataStorage;
import java.io.IOException;

/**
 * Application entry point dispatcher.
 *
 * Selects which subsystem to run based on the first command-line argument.
 * Passing "DataStorage" as the first argument reads simulator output files from the optional
 * directory, evaluates all alert conditions, and prints results.
 * Any other value or no argument starts the HealthDataSimulator with the remaining arguments
 * forwarded as-is.
 */
public class Main {

    /**
     * Dispatches to DataStorage or HealthDataSimulator depending on the first argument.
     *
     * @param args command-line arguments; first element (if present) selects the subsystem
     * @throws IOException if DataStorage mode is selected and the directory cannot be read
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            // Strip the "DataStorage" token and pass the rest to DataStorage.main
            String[] remaining = new String[args.length - 1];
            System.arraycopy(args, 1, remaining, 0, remaining.length);
            DataStorage.main(remaining);
        } else {
            HealthDataSimulator.main(args);
        }
    }
}
