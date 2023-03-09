package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.ControlToActuatorPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

public class LandingGearActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer landingGearConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.LANDING_GEAR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());

                    String landingGearState = packet.getValue() == 1 ? "DOWN" : "UP";
                    System.out.println("[i] Landing gear is set to " + landingGearState);

                    long sensorToControlResponseTime =
                            packet.getTimestampFromControl() - packet.getTimestampFromSensor();
                    long controlToActuatorResponseTime =
                            System.currentTimeMillis() - packet.getTimestampFromControl();

//                    try (FileWriter fw = new FileWriter(Constants.LANDING_GEAR_ROUTING_KEY + ".csv", true);
//                         BufferedWriter bw = new BufferedWriter(fw);
//                         PrintWriter out = new PrintWriter(bw)) {
//                        out.printf(
//                                "%s,%s,%d,%d,%d%n",
//                                packet.getSensor(),
//                                c.getRoutingKey(),
//                                sensorToControlResponseTime,
//                                controlToActuatorResponseTime,
//                                sensorToControlResponseTime + controlToActuatorResponseTime
//                        );
//                    } catch (IOException e) {
//                        //exception handling left as an exercise for the reader
//                    }
                })
                .build();

        new Thread(landingGearConsumer).start();
    }
}
