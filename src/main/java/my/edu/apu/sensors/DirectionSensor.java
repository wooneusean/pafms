package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.ActuatorToSensorPacket;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.SensoryToControlPacket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DirectionSensor {
    private static int currentDirection = 0;
    private static int currentTailAngle = 0;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher directionPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    long currentTimestamp = System.currentTimeMillis();
                    currentDirection = (int) (currentDirection + Math.floor((Math.random() * 100) - 50) +
                                              Math.floor(currentTailAngle / 45.0 * 100));

                    System.out.println(
                            "[.] Sending direction: " +
                            currentDirection +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(String.join(
                                "|",
                                Constants.DIRECTION_SENSOR_ROUTING_KEY,
                                String.valueOf(currentDirection),
                                String.valueOf(currentTimestamp)
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ExchangeConsumer directionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.ACTUATOR_TO_SENSOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.DIRECTION_SENSOR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    String[] packet = new String(delivery.getBody()).split("\\|");
                    currentTailAngle = Integer.parseInt(packet[1]);
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(directionPublisher, 0, 1, TimeUnit.SECONDS);

        new Thread(directionConsumer).start();
    }
}
