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

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer flightControlConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    long deliveryTimestamp = System.currentTimeMillis();
                    SensoryToControlPacket packet = Publishable.fromBytes(delivery.getBody());
                    try {
                        switch (packet.getRoutingKey()) {
                            case Constants.ALTITUDE_SENSOR_ROUTING_KEY -> altitudeSensorHandler(packet, deliveryTimestamp);
                            case Constants.SPEED_SENSOR_ROUTING_KEY -> speedSensorHandler(packet, deliveryTimestamp);
                            case Constants.DIRECTION_SENSOR_ROUTING_KEY -> directionSensorHandler(packet, deliveryTimestamp);
                            case Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY -> cabinPressureSensorHandler(packet, deliveryTimestamp);
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

    private static void cabinPressureSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws IOException, TimeoutException {
        ExchangePublisher oxygenMaskPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.OXYGEN_MASKS_ROUTING_KEY)
                .withMessageGenerator(p -> {
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
                        p.publish(new ControlToActuatorPacket(
                                packet.getRoutingKey(),
                                shouldDropOxygenMask,
                                packet.getTimestampFromSensor(),
                                deliveryTimestamp
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        // OR just create publisher object once and manually publish
        // oxygenMaskPublisher.publish();

        // TODO: optimization - set msg generator instead of creating new threads
//        oxygenMaskPublisher.setMessageGenerator(p -> {
//
//        });

        new Thread(oxygenMaskPublisher).start();
    }

    private static void directionSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws IOException, TimeoutException {
        ExchangePublisher tailFlapPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.TAIL_FLAPS_ROUTING_KEY)
                .withMessageGenerator(p -> {
                    int newTailAngle = 0;
                    if (packet.getValue() > 0) {
                        newTailAngle = -45;
                    } else if (packet.getValue() < 0) {
                        newTailAngle = 45;
                    }

                    try {
                        p.publish(new ControlToActuatorPacket(
                                packet.getRoutingKey(),
                                newTailAngle,
                                packet.getTimestampFromSensor(),
                                deliveryTimestamp
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(tailFlapPublisher).start();
    }

    private static void speedSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws IOException, TimeoutException {
        previousSpeed = packet.getValue();

        ExchangePublisher engineThrottlePublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.ENGINE_THROTTLE_ROUTING_KEY)
                .withMessageGenerator(p -> {
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
                        p.publish(new ControlToActuatorPacket(
                                packet.getRoutingKey(),
                                newEngineThrottle,
                                packet.getTimestampFromSensor(),
                                deliveryTimestamp
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(engineThrottlePublisher).start();
    }

    private static void altitudeSensorHandler(SensoryToControlPacket packet, long deliveryTimestamp) throws IOException, TimeoutException {
        previousAltitude = packet.getValue();

        ExchangePublisher wingFlapPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.WING_FLAPS_ROUTING_KEY)
                .withMessageGenerator(p -> {
                    try {
                        if (packet.getValue() <= 400 && targetAltitude == 0) {
                            p.publish(new ControlToActuatorPacket(
                                    packet.getRoutingKey(),
                                    1,
                                    packet.getTimestampFromSensor(),
                                    deliveryTimestamp
                            ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
                        } else {
                            p.publish(new ControlToActuatorPacket(
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
                        p.publish(new ControlToActuatorPacket(
                                packet.getRoutingKey(),
                                newWingAngle,
                                packet.getTimestampFromSensor(),
                                deliveryTimestamp
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(wingFlapPublisher).start();
    }
}
