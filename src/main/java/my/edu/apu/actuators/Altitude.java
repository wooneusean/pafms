package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.rabbitmq.ExchangeConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Altitude {
    AirplaneState airplaneState;

    public static void main(String[] args) throws IOException, TimeoutException {
//        String exchangeName = "sensory-to-controller";
//        Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
//        Channel ch = con.createChannel();
//        ch.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
//
//        String queueName = ch.queueDeclare().getQueue();
//        ch.queueBind(queueName, exchangeName, "");
//
//        ch.basicConsume(queueName, true, (consumerTag, msg) -> {
//            int newAltitude = Integer.parseInt(new String(msg.getBody(), StandardCharsets.UTF_8));
//            System.out.println(consumerTag + " > Message received: " + newAltitude);
////            Altitude.value = newAltitude;
//        }, (consumerTag) -> {
//            System.out.println(consumerTag + " > Message cancelled");
//        });
//        System.out.println("Receiver is listening on exchange " + exchangeName + " on queue " + queueName);
        ExchangeConsumer altitudeConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.ALTITUDE_CONSUMER_ROUTING_KEY)
                .withAutoAck(true)
                .withDeliveryCallback((consumerTag, delivery) -> {
                    System.out.println("Within altitudeConsumer delivery callback");
                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
                })
                .build();

        ExchangeConsumer cabinConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.CABIN_CONSUMER_ROUTING_KEY)
                .withAutoAck(true)
                .withDeliveryCallback((consumerTag, delivery) -> {
                    System.out.println("Within cabinConsumer delivery callback");
                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
                })
                .build();

        new Thread(altitudeConsumer).start();
        new Thread(cabinConsumer).start();
    }
}
