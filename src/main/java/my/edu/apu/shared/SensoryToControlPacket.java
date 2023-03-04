package my.edu.apu.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import my.edu.apu.rabbitmq.Publishable;

@Getter
@Setter
@AllArgsConstructor
public class SensoryToControlPacket implements Publishable {
    protected String routingKey;
    protected int value;
    protected long timestampFromSensor;

    @Override
    public String toString() {
        return "{\"SensoryToControlPacket\":{" +
               "\"routingKey\":\"" + routingKey + '\"' +
               ", \"value\":" + value +
               ", \"timestampFromSensor\":" + timestampFromSensor +
               "}}";
    }
}
