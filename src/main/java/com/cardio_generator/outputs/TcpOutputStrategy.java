package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Implements OutputStrategy by streaming patient data to a single TCP client.
 *
 * On construction a ServerSocket is opened on the specified port and a background
 * thread is started to accept the first incoming connection. Once a client connects, all
 * subsequent calls to write a comma-separated record to that client's socket.
 * Only one client is supported at a time; additional connection attempts are ignored until the
 * first client disconnects.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Creates a TCP server on the given port and begins listening for a client in the background.
     *
     * The constructor returns immediately; data is silently dropped until
     * the first client connects and the writer is initialised.
     *
     * @param port the TCP port number on which the server will listen, must be in the range
     *             1–65535 and must not already be in use
     * @throws RuntimeException wrapping an IOException if the server socket cannot be
     *                          created 
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a single patient data record over the open TCP connection.
     *
     * If no client has connected yet, this call is a no-op and the record is silently discarded.
     *
     * @param patientId the unique identifier of the patient whose data is being sent
     * @param timestamp the time of the measurement in milliseconds since the Unix epoch
     * @param label     the category of health data 
     * @param data      the measured value as a string
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
