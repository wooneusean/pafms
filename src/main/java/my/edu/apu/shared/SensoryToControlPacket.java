package my.edu.apu.shared;

import my.edu.apu.rabbitmq.Publishable;

public class SensoryToControlPacket implements Publishable {
    String routingKey;
    int value;
    public SensoryToControlPacket(String routingKey, int value) {
        this.routingKey = routingKey;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{\"SensoryToControlPacket\":{" +
               "\"routingKey\":\"" + routingKey + '\"' +
               ", \"value\":" + value +
               "}}";
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
