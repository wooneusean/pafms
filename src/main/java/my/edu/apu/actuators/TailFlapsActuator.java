package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.ActuatorToSensorPacket;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.ControlToActuatorPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

public class TailFlapsActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer tailFlapInstructionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.TAIL_FLAPS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());

                    System.out.printf(
                            "[.] Setting tail flap angle to '%s'%n",
                            packet.getValue()
                    );

                    long sensorToControlResponseTime =
                            packet.getTimestampFromControl() - packet.getTimestampFromSensor();
                    long controlToActuatorResponseTime =
                            System.currentTimeMillis() - packet.getTimestampFromControl();

//                    try (FileWriter fw = new FileWriter(Constants.TAIL_FLAPS_ROUTING_KEY + ".csv", true);
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

                    c.getChannel().basicPublish(
                            Constants.ACTUATOR_TO_SENSOR_EXCHANGE,
                            Constants.DIRECTION_SENSOR_ROUTING_KEY,
                            null,
                            new ActuatorToSensorPacket(
                                    Constants.TAIL_FLAPS_ROUTING_KEY,
                                    packet.getValue(),
                                    packet.getTimestampFromSensor(),
                                    packet.getTimestampFromControl(),
                                    System.currentTimeMillis()
                            ).getBytes()
                    );
                })
                .build();

        new Thread(tailFlapInstructionConsumer).start();
    }
}
