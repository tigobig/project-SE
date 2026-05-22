package data_management;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;

class SingletonTest {

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
    }

    @Test
    void testDataStorageGetInstanceReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void testDataStorageGetInstanceIsNotNull() {
        assertNotNull(DataStorage.getInstance());
    }

    @Test
    void testAfterResetGetInstanceReturnsDifferentInstance() {
        DataStorage first = DataStorage.getInstance();
        DataStorage.resetInstance();
        DataStorage second = DataStorage.getInstance();
        assertNotSame(first, second);
    }

    @Test
    void testDataStorageSingletonRetainsDataAcrossCalls() {
        DataStorage.getInstance().addPatientData(1, 100.0, "HeartRate", 1000L);
        assertEquals(1, DataStorage.getInstance().getRecords(1, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testHealthDataSimulatorGetInstanceReturnsSameInstance() {
        HealthDataSimulator a = HealthDataSimulator.getInstance();
        HealthDataSimulator b = HealthDataSimulator.getInstance();
        assertSame(a, b);
    }

    @Test
    void testHealthDataSimulatorGetInstanceIsNotNull() {
        assertNotNull(HealthDataSimulator.getInstance());
    }
}
