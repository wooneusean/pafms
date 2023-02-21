package my.edu.apu.flightcontroller;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.shared.Constants;
import my.edu.apu.rabbitmq.ExchangePublisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class FlightController {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher cabinPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
                .withMessageGenerator((publisher -> {
                    try {
                        while (true) {
                            publisher.publish("Hello cabin".getBytes());
                            Thread.sleep(2000);
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .withRoutingKey(Constants.CABIN_CONSUMER_ROUTING_KEY)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .build();


        ExchangePublisher altitudePublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
                .withMessageGenerator((publisher -> {
                    try {
                        while (true) {
                            publisher.publish("Hello altitude".getBytes());
                            Thread.sleep(500);
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .withRoutingKey(Constants.ALTITUDE_CONSUMER_ROUTING_KEY)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .build();

        new Thread(cabinPublisher).start();
        new Thread(altitudePublisher).start();
    }
}
