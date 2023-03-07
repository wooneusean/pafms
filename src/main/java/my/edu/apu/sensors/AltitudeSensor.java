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


public class AltitudeSensor {
    private static int currentWingAngle = 0;
    private static int currentAltitude = 25000;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher altitudePublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    currentAltitude = (int) (currentAltitude + Math.floor(Math.random() * -200) +
                                             Math.floor(currentWingAngle / 45.0 * 600));

                    if (currentAltitude <= 0) {
                        currentAltitude = 0;
                    }

                    System.out.println(
                            "[.] Sending altitude: " +
                            currentAltitude +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(new SensoryToControlPacket(
                                Constants.ALTITUDE_SENSOR_ROUTING_KEY,
                                currentAltitude,
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ExchangeConsumer altitudeConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.ACTUATOR_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.ALTITUDE_SENSOR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ActuatorToSensorPacket packet = Publishable.fromBytes(delivery.getBody());
                    currentWingAngle = packet.getValue();

                    // TODO: Figure out where to do the timestamp calculations
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(altitudePublisher, 0, 1, TimeUnit.SECONDS);

        new Thread(altitudeConsumer).start();
    }
}
