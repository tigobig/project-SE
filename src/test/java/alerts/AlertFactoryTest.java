package alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.BloodOxygenAlert;
import com.alerts.BloodPressureAlert;
import com.alerts.ECGAlert;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;

class AlertFactoryTest {

    @Test
    void testBloodPressureFactoryCreatesBloodPressureAlert() {
        Alert alert = new BloodPressureAlertFactory().createAlert("1", "Critical High SystolicPressure", 1000L);
        assertInstanceOf(BloodPressureAlert.class, alert);
    }

    @Test
    void testBloodOxygenFactoryCreatesBloodOxygenAlert() {
        Alert alert = new BloodOxygenAlertFactory().createAlert("2", "Low Blood Saturation", 2000L);
        assertInstanceOf(BloodOxygenAlert.class, alert);
    }

    @Test
    void testECGFactoryCreatesECGAlert() {
        Alert alert = new ECGAlertFactory().createAlert("3", "Abnormal ECG Peak", 3000L);
        assertInstanceOf(ECGAlert.class, alert);
    }

    @Test
    void testBloodPressureFactoryFieldsCorrect() {
        Alert alert = new BloodPressureAlertFactory().createAlert("42", "Critical Low SystolicPressure", 9999L);
        assertEquals("42", alert.getPatientId());
        assertEquals("Critical Low SystolicPressure", alert.getCondition());
        assertEquals(9999L, alert.getTimestamp());
    }

    @Test
    void testBloodOxygenFactoryFieldsCorrect() {
        Alert alert = new BloodOxygenAlertFactory().createAlert("7", "Rapid Blood Saturation Drop", 5000L);
        assertEquals("7", alert.getPatientId());
        assertEquals("Rapid Blood Saturation Drop", alert.getCondition());
        assertEquals(5000L, alert.getTimestamp());
    }

    @Test
    void testECGFactoryFieldsCorrect() {
        Alert alert = new ECGAlertFactory().createAlert("99", "Abnormal ECG Peak", 8000L);
        assertEquals("99", alert.getPatientId());
        assertEquals("Abnormal ECG Peak", alert.getCondition());
        assertEquals(8000L, alert.getTimestamp());
    }
}
