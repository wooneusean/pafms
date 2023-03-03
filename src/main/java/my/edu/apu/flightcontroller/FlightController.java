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
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    SensoryToControlPacket packet = Publishable.fromBytes(delivery.getBody());
                    try {
                        switch (packet.getRoutingKey()) {
                            case Constants.ALTITUDE_SENSOR_ROUTING_KEY -> altitudeSensorHandler(packet);
                            case Constants.SPEED_SENSOR_ROUTING_KEY -> speedSensorHandler(packet);
                            case Constants.DIRECTION_SENSOR_ROUTING_KEY -> directionSensorHandler(packet);
                            case Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY -> cabinPressureSensorHandler(packet);
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

    private static void cabinPressureSensorHandler(SensoryToControlPacket packet) throws IOException, TimeoutException {
        ExchangePublisher oxygenMaskPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.OXYGEN_MASKS_ROUTING_KEY)
                .withMessageGenerator(p -> {
                    int shouldDropOxygenMask = 0;

                    if (packet.getValue() >= 8000) {
                        shouldDropOxygenMask = 1;
                    }

                    try {
                        p.publish(new ControlToActuatorPacket(
                                shouldDropOxygenMask,
                                packet.getTimestamp(),
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(oxygenMaskPublisher).start();
    }

    private static void directionSensorHandler(SensoryToControlPacket packet) throws IOException, TimeoutException {
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
                                newTailAngle,
                                packet.getTimestamp(),
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(tailFlapPublisher).start();
    }

    private static void speedSensorHandler(SensoryToControlPacket packet) throws IOException, TimeoutException {
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
                                newEngineThrottle,
                                packet.getTimestamp(),
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(engineThrottlePublisher).start();
    }

    private static void altitudeSensorHandler(SensoryToControlPacket packet) throws IOException, TimeoutException {
        previousAltitude = packet.getValue();

        ExchangePublisher wingFlapPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.WING_FLAPS_ROUTING_KEY)
                .withMessageGenerator(p -> {
                    try {
                        if (packet.getValue() <= 400 && targetAltitude == 0) {
                            p.publish(new ControlToActuatorPacket(
                                    1,
                                    packet.getTimestamp(),
                                    System.currentTimeMillis()
                            ).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
                        } else {
                            p.publish(new ControlToActuatorPacket(
                                    0,
                                    packet.getTimestamp(),
                                    System.currentTimeMillis()
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
                                newWingAngle,
                                packet.getTimestamp(),
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(wingFlapPublisher).start();
    }
}
