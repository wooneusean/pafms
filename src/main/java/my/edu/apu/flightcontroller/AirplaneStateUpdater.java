package my.edu.apu.flightcontroller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.EnvironmentState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class AirplaneStateUpdater {
    public static volatile AirplaneStateUpdater instance;
    AirplaneState airplaneState = new AirplaneState(50, 0, 0, false, false);
    EnvironmentState environmentState = new EnvironmentState(10000, 0, 4000, 400);

    public static AirplaneStateUpdater getInstance() {
        AirplaneStateUpdater result = instance;
        if (result != null) {
            return result;
        }

        synchronized (AirplaneStateUpdater.class) {
            if (instance == null) {
                instance = new AirplaneStateUpdater();
            }
            return instance;
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer consumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY)
                .withDeliveryCallback((consumerTag, delivery) -> {
                    String value = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    switch (value) {
                        case Constants.ENGINE_THROTTLE:
                            AirplaneStateUpdater.getInstance().airplaneState.engineThrottle = Float.parseFloat(value);
                            break;
                        case Constants.WING_ANGLE:
                            break;
                        case Constants.TAIL_ANGLE:
                            break;
                        case Constants.LANDING_GEAR_DEPLOYED:
                            break;
                        case Constants.OXYGEN_MASKS_DEPLOYED:
                            break;
                        default:
                            break;
                    }
                })
                .build();

        ExchangePublisher publisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.TOPIC)
                .withRoutingKey("sensors.*")
                .withMessageGenerator(exchangePublisher -> {

                })
                .build();
    }
}
