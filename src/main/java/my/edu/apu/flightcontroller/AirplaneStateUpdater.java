package my.edu.apu.flightcontroller;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.AirplaneStateUpdaterPacket;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.EnvironmentState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AirplaneStateUpdater {
    public static volatile AirplaneStateUpdater instance;
    AirplaneState airplaneState = new AirplaneState(50, 0, 0, false, false);
    EnvironmentState environmentState = new EnvironmentState(10000, 0, 4000, 400);

    public static AirplaneStateUpdater getInstance() {
        AirplaneStateUpdater result = instance;
        if (result != null) {
            return result;
        }

        synchronized (AirplaneStateUpdater.class) {
            if (instance == null) {
                instance = new AirplaneStateUpdater();
            }
            return instance;
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        // This will be from actuators sending updated values for calculation here
//        ExchangeConsumer consumer = new ExchangeConsumer.Builder()
//                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE_NAME)
//                .withExchangeType(BuiltinExchangeType.DIRECT)
//                .withRoutingKey(Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY)
//                .withDeliveryCallback((consumerProps) -> (consumerTag, delivery) -> {
//                    AirplaneStateUpdaterPacket packet = AirplaneStateUpdaterPacket.fromBytes(delivery.getBody());
//                    System.out.println(
//                            "[.] " +
//                            Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY +
//                            " received: " +
//                            packet.toString()
//                    );
//                    switch (packet.getActuatorName()) {
//                        case Constants.ENGINE_THROTTLE:
//
//                            AirplaneStateUpdater.getInstance().airplaneState.engineThrottle += packet.getActuatorValue();
//                            System.out.println("Updating engine throttle, new engine throttle: " +
//                                               AirplaneStateUpdater.getInstance().airplaneState.engineThrottle);
//                            break;
//                        case Constants.WING_ANGLE:
//                            AirplaneStateUpdater.getInstance().airplaneState.wingAngle += packet.getActuatorValue();
//                            System.out.println("Updating wing angle, new wing angle: " +
//                                               AirplaneStateUpdater.getInstance().airplaneState.wingAngle);
//                            break;
//                        case Constants.TAIL_ANGLE:
//                            break;
//                        case Constants.LANDING_GEAR_DEPLOYED:
//                            break;
//                        case Constants.OXYGEN_MASKS_DEPLOYED:
//                            break;
//                        default:
//                            break;
//                    }
//                })
//                .build();
//
//        new Thread(consumer).start();

        /*
        This rpcConsumer will be the one responsible for sending updated
        sensory values to the sensors through RPC.
         */
        ExchangeConsumer rpcConsumer = new ExchangeConsumer.Builder()
                .withAutoAck(false)
                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE_NAME)
                .withRoutingKey(Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withDeliveryCallback((consumerData) -> (s, delivery) -> {
                    String sensor = new String(delivery.getBody(), StandardCharsets.UTF_8);

                    System.out.println("[.] " + Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY +
                                       " received: " +
                                       sensor);
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
                            .correlationId(delivery.getProperties().getCorrelationId())
                            .build();

                    switch (sensor) {
                        case Constants.ALTITUDE_CONSUMER_TOPIC -> {
                            // Could potentially modify values to include deviation
                            // before sending it back to sensory for it to react to.
                            consumerData.getChannel().basicPublish(
                                    consumerData.getExchangeName(),
                                    delivery.getProperties().getReplyTo(),
                                    replyProps,
                                    String.valueOf(AirplaneStateUpdater.getInstance().environmentState.altitude)
                                          .getBytes()
                            );
                            consumerData.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        }
                        case Constants.SPEED_CONSUMER_TOPIC -> {

                        }
                        default -> {
                        }
                    }
                })
                .build();

        new Thread(rpcConsumer).start();

//        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
//        exec.scheduleAtFixedRate(() -> {
//            // UPDATE ENVIRONMENT STATE HERE
//
//
//            // Speed difference values will come in and set the current throttle to it.
//            //
//            // Speed calculation:
//            // x = engineThrottle (in percent) / 100 * 400, 400 is max speed, maybe.
//            // add x to currentSpeed.
//            // currentSpeed always decreasing due to drag or some shit.
//            // Random value is used to decrease it?
//        }, 0, 1, TimeUnit.SECONDS);
    }
}
