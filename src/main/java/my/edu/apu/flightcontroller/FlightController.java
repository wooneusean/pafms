package my.edu.apu.flightcontroller;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class FlightController {
    static int targetAltitude = 25000;
    static int previousAltitude = -1;
    static int targetSpeed = 350;
    static int previousSpeed = -1;

    static ExchangePublisher oxygenMaskPublisher;
    static ExchangePublisher tailFlapPublisher;
    static ExchangePublisher engineThrottlePublisher;
    static ExchangePublisher wingFlapPublisher;

    static {
        try {
            oxygenMaskPublisher = new ExchangePublisher.Builder()
                    .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                    .withExchangeType(BuiltinExchangeType.DIRECT)
                    .withTargetRoutingKey(Constants.OXYGEN_MASKS_ROUTING_KEY)
                    .build();

            tailFlapPublisher = new ExchangePublisher.Builder()
                    .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                    .withExchangeType(BuiltinExchangeType.DIRECT)
                    .withTargetRoutingKey(Constants.TAIL_FLAPS_ROUTING_KEY)
                    .build();

            engineThrottlePublisher = new ExchangePublisher.Builder()
                    .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                    .withExchangeType(BuiltinExchangeType.DIRECT)
                    .withTargetRoutingKey(Constants.ENGINE_THROTTLE_ROUTING_KEY)
                    .build();

            wingFlapPublisher = new ExchangePublisher.Builder()
                    .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                    .withExchangeType(BuiltinExchangeType.DIRECT)
                    .withTargetRoutingKey(Constants.WING_FLAPS_ROUTING_KEY)
                    .build();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer flightControlConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    long deliveryTimestamp = System.currentTimeMillis();
                    // [routingKey, value, timestamp]
                    String[] message = new String(delivery.getBody(), StandardCharsets.UTF_8).split("\\|");
                    try {
                        switch (message[0]) {
                            case Constants.ALTITUDE_SENSOR_ROUTING_KEY -> altitudeSensorHandler(
                                    message,
                                    deliveryTimestamp
                            );
                            case Constants.SPEED_SENSOR_ROUTING_KEY -> speedSensorHandler(message, deliveryTimestamp);
                            case Constants.DIRECTION_SENSOR_ROUTING_KEY -> directionSensorHandler(
                                    message,
                                    deliveryTimestamp
                            );
                            case Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY -> cabinPressureSensorHandler(
                                    message,
                                    deliveryTimestamp
                            );
                            default -> System.out.printf(
                                    "[x] Unknown routing key provided: %s%n",
                                    message[0]
                            );
                        }
                    } catch (TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(flightControlConsumer).start();

        Scanner scn = new Scanner(System.in);
        System.out.println("[i] Press [enter] to trigger landing sequence.");
        scn.nextLine();
        targetAltitude = 0;
        targetSpeed = 0;
        System.out.println("[i] Landing sequence triggered.");
    }

    private static void cabinPressureSensorHandler(String[] message, long deliveryTimestamp) throws
                                                                                             TimeoutException {
        String shouldDropOxygenMask = "0";

        if (Integer.parseInt(message[1]) >= 8000) {
            shouldDropOxygenMask = "1";
            targetAltitude = 0;
            targetSpeed = 0;
            System.out.println(
                    "[i] Cabin pressure is getting dangerously high, dropping oxygen masks and triggering landing sequence."
            );
        }

        try {
            oxygenMaskPublisher.publish(String.join(
                    "|",
                    message[0],
                    shouldDropOxygenMask,
                    message[2],
                    String.valueOf(deliveryTimestamp)
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void directionSensorHandler(String[] message, long deliveryTimestamp) throws
                                                                                         TimeoutException {
        int newTailAngle = 0;
        int value = Integer.parseInt(message[1]);
        if (value > 0) {
            newTailAngle = -45;
        } else if (value < 0) {
            newTailAngle = 45;
        }

        try {
            tailFlapPublisher.publish(String.join(
                    "|",
                    message[0],
                    String.valueOf(newTailAngle),
                    message[2],
                    String.valueOf(deliveryTimestamp)
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void speedSensorHandler(String[] message, long deliveryTimestamp) throws
                                                                                     TimeoutException {
        int value = Integer.parseInt(message[1]);

        int newEngineThrottle = 50;

        // landing sequence
        if (targetSpeed == 0) {
            if (previousAltitude <= 0) {
                newEngineThrottle = 0;
            } else if (previousAltitude <= 2500) {
                newEngineThrottle = -100;
            } else if (previousAltitude <= 6000) {
                newEngineThrottle = 25;
            }
        } else {
            // normal sequence
            if (value > targetSpeed) {
                newEngineThrottle = 25;
            } else if (value < targetSpeed) {
                newEngineThrottle = 75;
            }
        }

        try {
            engineThrottlePublisher.publish(String.join(
                    "|",
                    message[0],
                    String.valueOf(newEngineThrottle),
                    message[2],
                    String.valueOf(deliveryTimestamp)
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        previousSpeed = value;
    }

    private static void altitudeSensorHandler(String[] message, long deliveryTimestamp) throws
                                                                                        TimeoutException {
        int value = Integer.parseInt(message[1]);

        try {
            if (value <= 400 && targetAltitude == 0) {
                wingFlapPublisher.publish(String.join(
                        "|",
                        message[0],
                        "1",
                        message[2],
                        String.valueOf(deliveryTimestamp)
                ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
            } else {
                wingFlapPublisher.publish(String.join(
                        "|",
                        message[0],
                        "0",
                        message[2],
                        String.valueOf(deliveryTimestamp)
                ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int newWingAngle = 0;
        if (value > targetAltitude + 100) {
            newWingAngle = -45;
        } else if (value < targetAltitude - 100) {
            newWingAngle = 45;
        }

        try {
            wingFlapPublisher.publish(String.join(
                    "|",
                    message[0],
                    String.valueOf(newWingAngle),
                    message[2],
                    String.valueOf(deliveryTimestamp)
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        previousAltitude = value;
    }
}
