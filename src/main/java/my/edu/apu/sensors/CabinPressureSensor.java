package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.SensoryToControlPacket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CabinPressureSensor {
    static int currentIterations = 0;
    static int currentCabinPressure = 6000;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher directionPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    long currentTimestamp = System.currentTimeMillis();
                    currentIterations++;

                    currentCabinPressure += Math.floor((Math.random() * 100) - 50);

//                    if (currentIterations > 60 && currentCabinPressure < 10000) {
//                        currentCabinPressure += Math.floor((Math.random() * 1000));
//                    } else {
//                        currentCabinPressure += Math.floor((Math.random() * 100) - 50);
//                    }

                    if (currentCabinPressure <= 0) {
                        currentCabinPressure = 0;
                    }

                    System.out.println(
                            "[.] Sending cabin pressure: " +
                            currentCabinPressure +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(String.join(
                                "|",
                                Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY,
                                String.valueOf(currentCabinPressure),
                                String.valueOf(currentTimestamp)
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(directionPublisher, 0, 1, TimeUnit.SECONDS);
    }
}
