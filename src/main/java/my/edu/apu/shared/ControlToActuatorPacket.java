package my.edu.apu.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import my.edu.apu.rabbitmq.Publishable;

/**
 * Represents the structure of object that gets sent from the flight controller to any one of the actuators.
 */
@Getter
@Setter
@AllArgsConstructor
public class ControlToActuatorPacket implements Publishable {
    protected String sensor;
    protected int value;
    protected long sensorToControlResponseTime;
    protected long timestampFromControl;

    @Override
    public String toString() {
        return "{\"ControlToActuatorPacket\":{" +
               "\"value\":" + value +
               ", \"sensorToControlResponseTime\":" + sensorToControlResponseTime +
               ", \"timestampFromControl\":" + timestampFromControl +
               "}}";
    }
}
