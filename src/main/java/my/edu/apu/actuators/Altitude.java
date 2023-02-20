package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import my.edu.apu.shared.AirplaneState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Altitude {
    AirplaneState airplaneState;

    public static void main(String[] args) throws IOException, TimeoutException {
        String exchangeName = "sensory-to-controller";
        ConnectionFactory cf = new ConnectionFactory();
        Connection con = cf.newConnection();
        Channel ch = con.createChannel();
        ch.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);

        String queueName = ch.queueDeclare().getQueue();
        ch.queueBind(queueName, exchangeName, "");

        ch.basicConsume(queueName, true, (consumerTag, msg) -> {
            int newAltitude = Integer.parseInt(new String(msg.getBody(), StandardCharsets.UTF_8));
            System.out.println(consumerTag + " > Message received: " + newAltitude);
            Altitude.value = newAltitude;
        }, (consumerTag) -> {
            System.out.println(consumerTag + " > Message cancelled");
        });
        System.out.println("Receiver is listening on exchange " + exchangeName + " on queue " + queueName);
    }
}
