package my.edu.apu.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import my.edu.apu.rabbitmq.Publishable;

@Getter
@Setter
@AllArgsConstructor
public class ControlToActuatorPacket implements Publishable {
    protected int value;
    protected long timestampFromSensor;
    protected long timestampFromControl;

    @Override
    public String toString() {
        return "{\"ControlToActuatorPacket\":{" +
               "\"value\":" + value +
               ", \"timestampFromSensor\":" + timestampFromSensor +
               ", \"timestampFromControl\":" + timestampFromControl +
               "}}";
    }
}
