package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Altitude {
    AirplaneState airplaneState;

    public static void main(String[] args) throws IOException, TimeoutException {

        ExchangeConsumer altitudeConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.TOPIC)
                .withRoutingKey(Constants.ALTITUDE_CONSUMER_TOPIC)
                .withAutoAck(true)
                .withDeliveryCallback((consumerTag, delivery) -> {
                    System.out.println("Within altitudeConsumer delivery callback");
                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
                })
                .build();

        new Thread(altitudeConsumer).start();

//        ExchangeConsumer cabinConsumer = new ExchangeConsumer.Builder()
//                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
//                .withExchangeType(BuiltinExchangeType.TOPIC)
//                .withRoutingKey(Constants.CABIN_CONSUMER_TOPIC)
//                .withAutoAck(true)
//                .withDeliveryCallback((consumerTag, delivery) -> {
//                    System.out.println("Within cabinConsumer delivery callback");
//                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
//                })
//                .build();

//        new Thread(cabinConsumer).start();
    }
}
