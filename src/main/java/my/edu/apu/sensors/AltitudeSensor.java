package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.ActuatorToSensorPacket;
import my.edu.apu.shared.Constants;

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
                    long currentTimestamp = System.currentTimeMillis();
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
                        publisher.publish(String.join(
                                "|",
                                Constants.ALTITUDE_SENSOR_ROUTING_KEY,
                                String.valueOf(currentAltitude),
                                String.valueOf(currentTimestamp)
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ExchangeConsumer altitudeConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.ACTUATOR_TO_SENSOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.ALTITUDE_SENSOR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    String[] packet = new String(delivery.getBody()).split("\\|");
                    currentWingAngle = Integer.parseInt(packet[1]);
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(altitudePublisher, 0, 1, TimeUnit.SECONDS);

        new Thread(altitudeConsumer).start();
    }
}
