package my.edu.apu.flightcontroller;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.SensoryToControlPacket;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class FlightController {
    static int targetAltitude = 25000;
    static int targetSpeed = 350;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer flightControlConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    SensoryToControlPacket packet = Publishable.fromBytes(delivery.getBody());
                    switch (packet.getRoutingKey()) {
                        case Constants.ALTITUDE_SENSOR_ROUTING_KEY -> {
                            try {
                                handleAltitudeValue(packet.getValue());
                            } catch (TimeoutException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        default -> System.out.printf("[x] Unknown routing key provided: %s%n", packet.getRoutingKey());
                    }
                })
                .build();

        new Thread(flightControlConsumer).start();

        Scanner scn = new Scanner(System.in);
        System.out.println("[i] Press [enter] to trigger landing sequence.");
        scn.nextLine();
        targetAltitude = 0;
        System.out.println("[i] Landing sequence triggered.");
    }

    private static void handleAltitudeValue(int value) throws IOException, TimeoutException {
        ExchangePublisher wingFlapPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.WING_FLAPS_ROUTING_KEY)
                .withMessageGenerator(p -> {
                    if (value <= 400) {
                        try {
                            p.publish(String.valueOf(1).getBytes(), Constants.LANDING_GEAR_ROUTING_KEY);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    int newWingAngle = 0;
                    if (value > targetAltitude) {
                        newWingAngle = -45;
                    } else if (value < targetAltitude) {
                        newWingAngle = 45;
                    } else if (value == 0) {
                        System.out.println("[i] Plane has landed.");
                        return;
                    }

                    try {
                        p.publish(String.valueOf(newWingAngle).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        new Thread(wingFlapPublisher).start();
    }
}
