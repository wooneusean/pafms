package my.edu.apu.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import my.edu.apu.rabbitmq.Publishable;

@Getter
@Setter
@AllArgsConstructor
public class ActuatorToSensorPacket implements Publishable {
    protected String actuator;
    protected int value;
    protected long timestampFromSensor;
    protected long timestampFromControl;
    protected long timestampFromActuator;

    @Override
    public String toString() {
        return "{\"ActuatorToSensorPacket\":{" +
               "\"actuator\":\"" + actuator + '\"' +
               ", \"value\":" + value +
               ", \"timestampFromSensor\":" + timestampFromSensor +
               ", \"timestampFromControl\":" + timestampFromControl +
               ", \"timestampFromActuator\":" + timestampFromActuator +
               "}}";
    }
}
