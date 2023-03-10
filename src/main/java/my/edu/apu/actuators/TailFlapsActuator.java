package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.shared.Constants;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TailFlapsActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer tailFlapInstructionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.TAIL_FLAPS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    // [sensorRoutingKey, value, timestampFromSensor, timestampFromControl]
                    String[] message = new String(delivery.getBody(), StandardCharsets.UTF_8).split("\\|");

                    System.out.printf(
                            "[.] Setting tail flap angle to '%s'%n",
                            message[1]
                    );

//                    long timestampFromControl = Long.parseLong(message[3]);
//                    long timestampFromSensor = Long.parseLong(message[2]);
//                    long sensorToControlResponseTime = timestampFromControl - timestampFromSensor;
//                    long controlToActuatorResponseTime = System.currentTimeMillis() - timestampFromControl;
//
//                    try (FileWriter fw = new FileWriter(c.getRoutingKey() + ".csv", true);
//                         BufferedWriter bw = new BufferedWriter(fw);
//                         PrintWriter out = new PrintWriter(bw)) {
//                        out.printf(
//                                "%s,%s,%d,%d,%d%n",
//                                message[0],
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
                            String.join(
                                    "|",
                                    Constants.TAIL_FLAPS_ROUTING_KEY,
                                    message[1],
                                    message[2],
                                    message[3],
                                    String.valueOf(System.currentTimeMillis())
                            ).getBytes()
                    );
                })
                .build();

        new Thread(tailFlapInstructionConsumer).start();
    }
}
