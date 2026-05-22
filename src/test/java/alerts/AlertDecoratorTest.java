package alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.decorators.AlertDecorator;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;

class AlertDecoratorTest {

    private Alert base(String condition) {
        return new Alert("1", condition, 1000L);
    }

    // PriorityAlertDecorator

    @Test
    void testPriorityDecoratorPrependsTag() {
        Alert decorated = new PriorityAlertDecorator(base("Low Blood Saturation"), "HIGH");
        assertEquals("[HIGH] Low Blood Saturation", decorated.getCondition());
    }

    @Test
    void testPriorityDecoratorPreservesPatientId() {
        Alert decorated = new PriorityAlertDecorator(base("Low Blood Saturation"), "HIGH");
        assertEquals("1", decorated.getPatientId());
    }

    @Test
    void testPriorityDecoratorPreservesTimestamp() {
        Alert decorated = new PriorityAlertDecorator(base("Low Blood Saturation"), "HIGH");
        assertEquals(1000L, decorated.getTimestamp());
    }

    @Test
    void testPriorityDecoratorGetPriorityLevel() {
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base("Low Blood Saturation"), "HIGH");
        assertEquals("HIGH", decorated.getPriorityLevel());
    }

    // RepeatedAlertDecorator

    @Test
    void testRepeatedDecoratorAppendsCount() {
        Alert decorated = new RepeatedAlertDecorator(base("Abnormal ECG Peak"), 3);
        assertEquals("Abnormal ECG Peak (repeated 3 times)", decorated.getCondition());
    }

    @Test
    void testRepeatedDecoratorPreservesPatientId() {
        Alert decorated = new RepeatedAlertDecorator(base("Abnormal ECG Peak"), 3);
        assertEquals("1", decorated.getPatientId());
    }

    @Test
    void testRepeatedDecoratorPreservesTimestamp() {
        Alert decorated = new RepeatedAlertDecorator(base("Abnormal ECG Peak"), 3);
        assertEquals(1000L, decorated.getTimestamp());
    }

    @Test
    void testRepeatedDecoratorGetRepeatCount() {
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base("Abnormal ECG Peak"), 3);
        assertEquals(3, decorated.getRepeatCount());
    }

    // Stacked decorators

    @Test
    void testStackedDecorators() {
        Alert repeated = new RepeatedAlertDecorator(base("Low Blood Saturation"), 2);
        Alert stacked = new PriorityAlertDecorator(repeated, "HIGH");
        assertEquals("[HIGH] Low Blood Saturation (repeated 2 times)", stacked.getCondition());
    }

    // instanceof check

    @Test
    void testAlertDecoratorIsInstanceOfAlert() {
        AlertDecorator decorated = new AlertDecorator(base("Low Blood Saturation"));
        assertInstanceOf(Alert.class, decorated);
    }
}
