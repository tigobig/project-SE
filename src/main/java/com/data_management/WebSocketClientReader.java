package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;

/**
 * Connects to a WebSocket server and receives real-time patient data messages,
 * parsing each message and storing the resulting record in DataStorage.
 *
 * Messages must follow the format: patientId,timestamp,label,data
 * For example: 1,1714376789050,HeartRate,72.0
 *
 * Saturation values ending with a percent sign have the sign stripped before parsing.
 * The tokens "triggered" and "resolved" are mapped to 1.0 and 0.0 respectively.
 */
public class WebSocketClientReader extends WebSocketClient implements DataReader {

    private volatile DataStorage dataStorage;

    /**
     * Creates a WebSocketClientReader that will connect to the given server URI.
     *
     * @param serverUri the URI of the WebSocket server to connect to
     */
    public WebSocketClientReader(URI serverUri) {
        super(serverUri);
    }

    /**
     * Stores the given DataStorage so that subsequent messages are saved to it.
     * Call this before connecting if you need to set storage without triggering a connection.
     *
     * @param dataStorage the storage instance to receive incoming records
     */
    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Stores the DataStorage reference and establishes a blocking connection to the
     * WebSocket server. Returns once the connection is open and the client is ready
     * to receive messages.
     *
     * @param dataStorage the storage instance that will receive incoming patient records
     * @throws IOException if the connection cannot be established or is interrupted
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.dataStorage = dataStorage;
        try {
            boolean connected = connectBlocking();
            if (!connected) {
                throw new IOException("Failed to connect to WebSocket server at " + getURI());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection to WebSocket server was interrupted", e);
        }
    }

    /**
     * Called when the connection to the server is successfully opened.
     *
     * @param handshake the server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to WebSocket server at " + getURI());
    }

    /**
     * Called when a message arrives from the server. Parses the message and stores
     * the record in DataStorage. Malformed or unparseable messages are skipped with
     * an error logged to stderr.
     *
     * @param message the raw text message received from the server
     */
    @Override
    public void onMessage(String message) {
        if (dataStorage == null) {
            System.err.println("DataStorage not set; dropping message: " + message);
            return;
        }
        parseAndStore(message);
    }

    /**
     * Parses a single comma-separated message and adds the resulting record to storage.
     * Public to allow direct testing without a network connection.
     *
     * @param message the raw message string to parse
     */
    public void parseAndStore(String message) {
        try {
            String[] parts = message.split(",", 4);
            if (parts.length < 4) {
                System.err.println("Skipping malformed message (too few fields): " + message);
                return;
            }
            int patientId  = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label   = parts[2].trim();
            String dataStr = parts[3].trim();
            double value   = parseValue(dataStr);
            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            System.err.println("Invalid numeric data in message: " + message);
        } catch (Exception e) {
            System.err.println("Error processing message: " + message + " - " + e.getMessage());
        }
    }

    private double parseValue(String dataStr) {
        if (dataStr.endsWith("%")) {
            return Double.parseDouble(dataStr.substring(0, dataStr.length() - 1));
        }
        if ("triggered".equalsIgnoreCase(dataStr)) return 1.0;
        if ("resolved".equalsIgnoreCase(dataStr))  return 0.0;
        try {
            return Double.parseDouble(dataStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Called when the connection to the server is closed.
     *
     * @param code   the closure code
     * @param reason a human-readable reason for the closure
     * @param remote true if the server initiated the close
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed (code " + code + "): " + reason);
    }

    /**
     * Called when a WebSocket error occurs. Logs the error message to stderr.
     *
     * @param ex the exception that describes the error
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " +
            (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
    }
}
