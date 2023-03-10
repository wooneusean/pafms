package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpeedSensor {
    private static int currentSpeed = 350;
    private static int currentEngineThrottle = 50;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher speedPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    long currentTimestamp = System.currentTimeMillis();
                    currentSpeed = (int) (currentSpeed + Math.floor(Math.random() * -100) +
                                          Math.floor(currentEngineThrottle / 100.0 * 100));

                    if (currentSpeed <= 0) {
                        currentSpeed = 0;
                    }

                    System.out.println(
                            "[.] Sending speed: " +
                            currentSpeed +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(String.join(
                                "|",
                                Constants.SPEED_SENSOR_ROUTING_KEY,
                                String.valueOf(currentSpeed),
                                String.valueOf(currentTimestamp)
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ExchangeConsumer speedConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.ACTUATOR_TO_SENSOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.SPEED_SENSOR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    // [actuatorRoutingKey, value, timestampFromSensor, timestampFromControl, timestampFromActuator]
                    String[] packet = new String(delivery.getBody()).split("\\|");
                    currentEngineThrottle = Integer.parseInt(packet[1]);
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(speedPublisher, 0, 1, TimeUnit.SECONDS);

        new Thread(speedConsumer).start();
    }
}
