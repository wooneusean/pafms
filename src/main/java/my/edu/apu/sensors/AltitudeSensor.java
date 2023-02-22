package my.edu.apu.sensors;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.shared.Constants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;

public class AltitudeSensor {
    private static volatile int altitude = 0;

    public static void main(String[] args) throws IOException, TimeoutException {

//        ExchangeConsumer altitudeConsumer = new ExchangeConsumer.Builder()
//                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE_NAME)
//                .withExchangeType(BuiltinExchangeType.TOPIC)
//                .withRoutingKey(Constants.ALTITUDE_CONSUMER_TOPIC)
//                .withDeliveryCallback(consumerProps -> (consumerTag, delivery) -> {
//                    System.out.println("Within altitudeConsumer delivery callback");
//                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
//                })
//                .build();
//
//        new Thread(altitudeConsumer).start();

        ExchangePublisher sensoryRpcPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.AIRPLANE_STATE_UPDATER_EXCHANGE_NAME)
                .withRoutingKey(Constants.AIRPLANE_STATE_UPDATE_ROUTING_KEY)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withBasicPropertiesProvider(exchangePublisher -> {
                    try {
                        final String corrId = UUID.randomUUID().toString();
                        exchangePublisher.getUserData().put("corrId", corrId);
                        return new AMQP.BasicProperties.Builder()
                                .correlationId(corrId)
                                .replyTo(exchangePublisher.getChannel().queueDeclare().getQueue())
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withMessageGenerator(exchangePublisher -> {
                    try {
                        exchangePublisher.publish(Constants.ALTITUDE_CONSUMER_TOPIC.getBytes());
                        final CompletableFuture<String> response = new CompletableFuture<>();

                        // TODO: Not working, maybe due to this?
                        String ctag = exchangePublisher.getChannel().basicConsume(
                                exchangePublisher.getChannel()
                                                 .queueDeclare()
                                                 .getQueue(),
                                true,
                                (consumerTag, delivery) -> {
                                    if (delivery.getProperties()
                                                .getCorrelationId()
                                                .equals(exchangePublisher.getUserData().get("corrId"))) {
                                        String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                                        System.out.println(
                                                "[.] Received from " +
                                                delivery.getEnvelope().getRoutingKey() +
                                                ": " +
                                                msg
                                        );
                                        response.complete(msg);
                                    }
                                },
                                (consumerTag) -> {

                                }
                        );
                        altitude = Integer.parseInt(response.get());
                        exchangePublisher.getChannel().basicCancel(ctag);
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();


        ScheduledExecutorService exec = Executors.newScheduledThreadPool(2);
        exec.scheduleAtFixedRate(
                () -> System.out.println(altitude),
                500,
                1000,
                TimeUnit.MILLISECONDS
        );

        exec.scheduleAtFixedRate(
                sensoryRpcPublisher,
                0,
                1000,
                TimeUnit.MILLISECONDS
        );

//        ExchangeConsumer cabinConsumer = new ExchangeConsumer.Builder()
//                .withExchangeName(Constants.SENSORY_TO_CONTROLLER_EXCHANGE_NAME)
//                .withExchangeType(BuiltinExchangeType.TOPIC)
//                .withRoutingKey(Constants.CABIN_CONSUMER_TOPIC)
//                .withAutoAck(true)
//                .withDeliveryCallback((consumerTag, delivery) -> {
//                    System.out.println("Within cabinConsumer delivery callback");
//                    System.out.println("Receive message: " + new String(delivery.getBody(), StandardCharsets.UTF_8));
//                })
//                .build();

//        new Thread(cabinConsumer).start();
    }
}
