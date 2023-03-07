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
    private static int currentDirection;
    private static int currentTailAngle;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher directionPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    currentDirection = (int) (currentDirection + Math.floor((Math.random() * 100) - 50) +
                                              Math.floor(currentTailAngle / 45.0 * 100));

                    System.out.println(
                            "[.] Sending direction: " +
                            currentDirection +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(new SensoryToControlPacket(
                                Constants.DIRECTION_SENSOR_ROUTING_KEY,
                                currentDirection,
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ExchangeConsumer directionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.ACTUATOR_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.DIRECTION_SENSOR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ActuatorToSensorPacket packet = Publishable.fromBytes(delivery.getBody());
                    currentTailAngle = packet.getValue();

                    // TODO: Figure out where to do the timestamp calculations
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(directionPublisher, 0, 1, TimeUnit.SECONDS);

        new Thread(directionConsumer).start();
    }
}
