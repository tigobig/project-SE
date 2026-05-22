package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * An output strategy that starts an embedded WebSocket server and broadcasts
 * every generated patient data point to all currently connected clients.
 *
 * Each message is sent as a single line in the format:
 *   patientId,timestamp,label,data
 * For example: 1,1714376789050,HeartRate,72.0
 *
 * Clients connect to the server on the port supplied to the constructor and
 * receive every subsequent broadcast automatically.
 */
public class WebSocketOutputStrategy implements OutputStrategy {

    private WebSocketServer server;

    /**
     * Creates and starts a WebSocket server listening on the given port.
     * The server begins accepting client connections immediately after construction.
     *
     * @param port the TCP port the WebSocket server should listen on
     */
    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port + ", listening for connections...");
        server.start();
    }

    /**
     * Formats the supplied data as a comma-separated message and sends it to every
     * connected WebSocket client.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time of the measurement in milliseconds since the Unix epoch
     * @param label     the name of the measured health metric, for example HeartRate
     * @param data      the measurement value as a string, for example 72.0
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
    }

    private static class SimpleWebSocketServer extends WebSocketServer {

        /**
         * Creates an inner WebSocket server bound to the given address.
         *
         * @param address the local address and port to bind to
         */
        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        /**
         * Called when a new client connects. Logs the remote address.
         *
         * @param conn      the newly opened connection
         * @param handshake the client handshake data
         */
        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        /**
         * Called when a client disconnects. Logs the remote address.
         *
         * @param conn   the connection that was closed
         * @param code   the closure code sent by the client
         * @param reason a human-readable reason for the closure
         * @param remote true if the client initiated the close
         */
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        /**
         * Called when a message is received from a client.
         * This server only broadcasts outbound data and does not process inbound messages.
         *
         * @param conn    the connection that sent the message
         * @param message the message text received from the client
         */
        @Override
        public void onMessage(WebSocket conn, String message) {
            // This server only sends data; inbound messages are not processed.
        }

        /**
         * Called when a network or protocol error occurs on a connection.
         * Prints the stack trace to aid debugging.
         *
         * @param conn the connection on which the error occurred, may be null
         * @param ex   the exception describing the error
         */
        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        /**
         * Called once after the server socket is bound and the server is ready to accept
         * incoming connections.
         */
        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}
