package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class WingFlaps {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer wingFlapInstructionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.WING_FLAPS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.printf("[.] Received message > '%s'%n", msg);
                })
                .build();

        new Thread(wingFlapInstructionConsumer).start();
    }
}
