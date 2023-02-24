package my.edu.apu.rabbitmq;

import com.rabbitmq.client.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Getter
@Setter
public class ExchangeConsumer implements Runnable {
    protected String exchangeName;
    protected Channel channel;
    protected BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    protected String routingKey = "";
    protected String queueName = "";
    protected boolean autoAck = true;
    protected DeliverCallback deliveryCallback = (consumerTag, delivery) -> {
    };
    protected CancelCallback cancelCallback = (consumerTag) -> {
    };

    @Override
    public void run() {
        try {
            channel.basicConsume(queueName, autoAck, deliveryCallback, cancelCallback);
            System.out.printf("[.] %s is listening on exchange '%s' for messages.%n", routingKey, exchangeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        protected String exchangeName;
        protected BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
        protected String routingKey = "";
        protected boolean autoAck = true;
        protected Function<ExchangeConsumer, DeliverCallback> deliveryCallbackProvider = builder -> (s, delivery) -> {
        };
        protected Function<ExchangeConsumer, CancelCallback> cancelCallbackProvider = builder -> s -> {
        };

        public Builder() {
        }

        public Builder withExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public Builder withExchangeType(BuiltinExchangeType exchangeType) {
            this.exchangeType = exchangeType;
            return this;
        }

        public Builder withRoutingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public Builder withAutoAck(boolean autoAck) {
            this.autoAck = autoAck;
            return this;
        }

        public Builder withDeliveryCallback(Function<ExchangeConsumer, DeliverCallback> deliveryCallbackProvider) {
            this.deliveryCallbackProvider = deliveryCallbackProvider;
            return this;
        }

        public Builder withCancelCallback(Function<ExchangeConsumer, CancelCallback> cancelCallbackProvider) {
            this.cancelCallbackProvider = cancelCallbackProvider;
            return this;
        }

        public ExchangeConsumer build() throws IOException, TimeoutException {
            ExchangeConsumer consumer = new ExchangeConsumer();
            consumer.setExchangeName(exchangeName);
            consumer.setExchangeType(exchangeType);
            consumer.setRoutingKey(routingKey);
            consumer.setAutoAck(autoAck);

            Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
            consumer.setChannel(con.createChannel());
            consumer.getChannel().exchangeDeclare(exchangeName, exchangeType);

            consumer.setQueueName(consumer.getChannel().queueDeclare().getQueue());
            consumer.getChannel().queueBind(consumer.getQueueName(), exchangeName, routingKey);

            consumer.setDeliveryCallback(deliveryCallbackProvider.apply(consumer));
            consumer.setCancelCallback(cancelCallbackProvider.apply(consumer));
            return consumer;
        }
    }
}
