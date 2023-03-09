package my.edu.apu.flightcontroller;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.ControlToActuatorPacket;
import my.edu.apu.shared.SensoryToControlPacket;

import java.io.IOException;
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
        // TODO: Change packet objects to strings
        ExchangeConsumer flightControlConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    long deliveryTimestamp = System.currentTimeMillis();
                    SensoryToControlPacket packet = Publishable.fromBytes(delivery.getBody());
                    try {
                        switch (packet.getRoutingKey()) {
                            case Constants.ALTITUDE_SENSOR_ROUTING_KEY -> altitudeSensorHandler(
                                    packet,
                                    deliveryTimestamp
                            );
                            case Constants.SPEED_SENSOR_ROUTING_KEY -> speedSensorHandler(packet, deliveryTimestamp);
                            case Constants.DIRECTION_SENSOR_ROUTING_KEY -> directionSensorHandler(
                                    packet,
                                    deliveryTimestamp
                            );
                            case Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY -> cabinPressureSensorHandler(
                                    packet,
                                    deliveryTimestamp
                            );
                            default -> System.out.printf(
                                    "[x] Unknown routing key provided: %s%n",
                                    packet.getRoutingKey()
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

    private static void cabinPressureSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws
                                                                                                          TimeoutException {
        int shouldDropOxygenMask = 0;

        if (packet.getValue() >= 8000) {
            shouldDropOxygenMask = 1;
            targetAltitude = 0;
            targetSpeed = 0;
            System.out.println(
                    "[i] Cabin pressure is getting dangerously high, dropping oxygen masks and triggering landing sequence."
            );
        }

        try {
            oxygenMaskPublisher.publish(new ControlToActuatorPacket(
                    packet.getRoutingKey(),
                    shouldDropOxygenMask,
                    packet.getTimestampFromSensor(),
                    deliveryTimestamp
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void directionSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws
                                                                                                      TimeoutException {
        int newTailAngle = 0;
        if (packet.getValue() > 0) {
            newTailAngle = -45;
        } else if (packet.getValue() < 0) {
            newTailAngle = 45;
        }

        try {
            tailFlapPublisher.publish(new ControlToActuatorPacket(
                    packet.getRoutingKey(),
                    newTailAngle,
                    packet.getTimestampFromSensor(),
                    deliveryTimestamp
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void speedSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws
                                                                                                  TimeoutException {
        previousSpeed = packet.getValue();

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
            if (packet.getValue() > targetSpeed) {
                newEngineThrottle = 25;
            } else if (packet.getValue() < targetSpeed) {
                newEngineThrottle = 75;
            }
        }

        try {
            engineThrottlePublisher.publish(new ControlToActuatorPacket(
                    packet.getRoutingKey(),
                    newEngineThrottle,
                    packet.getTimestampFromSensor(),
                    deliveryTimestamp
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void altitudeSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws
                                                                                                     TimeoutException {
        previousAltitude = packet.getValue();

        try {
            if (packet.getValue() <= 400 && targetAltitude == 0) {
                wingFlapPublisher.publish(new ControlToActuatorPacket(
                        packet.getRoutingKey(),
                        1,
                        packet.getTimestampFromSensor(),
                        deliveryTimestamp
                ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
            } else {
                wingFlapPublisher.publish(new ControlToActuatorPacket(
                        packet.getRoutingKey(),
                        0,
                        packet.getTimestampFromSensor(),
                        deliveryTimestamp
                ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int newWingAngle = 0;
        if (packet.getValue() > targetAltitude + 100) {
            newWingAngle = -45;
        } else if (packet.getValue() < targetAltitude - 100) {
            newWingAngle = 45;
        }

        try {
            wingFlapPublisher.publish(new ControlToActuatorPacket(
                    packet.getRoutingKey(),
                    newWingAngle,
                    packet.getTimestampFromSensor(),
                    deliveryTimestamp
            ).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
