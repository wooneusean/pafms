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

public class OxygenMasksActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer oxygenMaskConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.OXYGEN_MASKS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());

                    System.out.printf(
                            "[.] Setting oxygen masks to: %s %n",
                            packet.getValue() == 0 ? "hidden" : "deployed"
                    );

//                    long sensorToControlResponseTime =
//                            packet.getTimestampFromControl() - packet.getTimestampFromSensor();
//                    long controlToActuatorResponseTime =
//                            System.currentTimeMillis() - packet.getTimestampFromControl();
//
//                    try (FileWriter fw = new FileWriter(Constants.OXYGEN_MASKS_ROUTING_KEY + ".csv", true);
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

        new Thread(oxygenMaskConsumer).start();
    }
}
