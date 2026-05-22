package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketClientReader;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketIntegrationTest {

    private static final String HOST = "localhost";

    private TestWebSocketServer testServer;
    private DataStorage storage;
    private LatchedClientReader client;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null && client.isOpen()) {
            client.closeBlocking();
        }
        if (testServer != null) {
            testServer.stop(2000);
        }
    }

    @Test
    void testServerSendsMessageClientStoresRecord() throws Exception {
        int port = 18701;
        CountDownLatch latch = new CountDownLatch(1);
        testServer = new TestWebSocketServer(port, new String[]{"1,5000,HeartRate,75.0"});
        testServer.start();
        testServer.waitUntilReady();

        client = new LatchedClientReader(new URI("ws://" + HOST + ":" + port), latch);
        client.readData(storage);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Timed out waiting for message");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test
    void testClientHandlesMultipleMessages() throws Exception {
        int port = 18702;
        CountDownLatch latch = new CountDownLatch(3);
        testServer = new TestWebSocketServer(port,
                "1,1000,HeartRate,70.0",
                "1,2000,HeartRate,72.0",
                "1,3000,HeartRate,74.0");
        testServer.start();
        testServer.waitUntilReady();

        client = new LatchedClientReader(new URI("ws://" + HOST + ":" + port), latch);
        client.readData(storage);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Timed out waiting for messages");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(3, records.size());
    }

    @Test
    void testClientHandlesMalformedMessageWithoutCrashing() throws Exception {
        int port = 18703;
        CountDownLatch latch = new CountDownLatch(1);
        testServer = new TestWebSocketServer(port, "bad,data", "1,1000,HeartRate,65.0");
        testServer.start();
        testServer.waitUntilReady();

        client = new LatchedClientReader(new URI("ws://" + HOST + ":" + port), latch);
        client.readData(storage);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Timed out waiting for messages");

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(65.0, records.get(0).getMeasurementValue(), 0.001);
    }

    // -----------------------------------------------------------------------
    // Client subclass that counts down a latch each time a valid record is stored.
    // -----------------------------------------------------------------------
    private static class LatchedClientReader extends WebSocketClientReader {

        private final CountDownLatch latch;

        LatchedClientReader(URI uri, CountDownLatch latch) {
            super(uri);
            this.latch = latch;
        }

        @Override
        public void parseAndStore(String message) {
            int before = getStorage().getAllPatients().stream()
                    .mapToInt(p -> getStorage().getRecords(p.getPatientId(), 0, Long.MAX_VALUE).size())
                    .sum();
            super.parseAndStore(message);
            int after = getStorage().getAllPatients().stream()
                    .mapToInt(p -> getStorage().getRecords(p.getPatientId(), 0, Long.MAX_VALUE).size())
                    .sum();
            if (after > before) {
                latch.countDown();
            }
        }

        private DataStorage getStorage() {
            return DataStorage.getInstance();
        }
    }

    // -----------------------------------------------------------------------
    // Helper server that sends a fixed set of messages once a client connects.
    // -----------------------------------------------------------------------
    private static class TestWebSocketServer extends WebSocketServer {

        private final String[] messages;
        private final CountDownLatch startLatch = new CountDownLatch(1);

        TestWebSocketServer(int port, String... messages) {
            super(new InetSocketAddress(port));
            this.messages = messages;
            setReuseAddr(true);
        }

        void waitUntilReady() throws InterruptedException {
            startLatch.await(3, TimeUnit.SECONDS);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            new Thread(() -> {
                for (String msg : messages) {
                    conn.send(msg);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

        @Override
        public void onMessage(WebSocket conn, String message) {}

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            startLatch.countDown();
        }
    }
}
