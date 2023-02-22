package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EngineActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer consumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROLLER_TO_ACTUATOR_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.ENGINE_ACTUATOR_ROUTING_KEY)
                .withDeliveryCallback(consumerProps -> (s, delivery) -> {
                    // Get 3 possible values from controller: -
                    //  - EngineAction.SLOW_DOWN
                    //  - EngineAction.SPEED_UP
                    //  - EngineAction.CONSTANT
                    // Then based on that, send fixed values to AirplaneStateUpdater
                    //  - SLOW_DOWN -> -25%
                    //  - SPEED_UP -> +75%
                    //  - CONSTANT -> 0%
                })
                .build();

        new Thread(consumer).start();
    }
}
