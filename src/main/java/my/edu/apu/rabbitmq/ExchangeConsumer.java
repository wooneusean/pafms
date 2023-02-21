package my.edu.apu.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ExchangeConsumer implements Runnable {
    public String exchangeName;
    public Channel channel;
    public BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    public String routingKey = "";
    public String queueName = "";
    public boolean autoAck = true;
    public DeliverCallback deliveryCallback = (consumerTag, delivery) -> {
    };
    public CancelCallback cancelCallback = (consumerTag) -> {
    };

    @Override
    public void run() {
        try {
            channel.basicConsume(queueName, autoAck, deliveryCallback, cancelCallback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        String exchangeName;
        Channel channel;
        BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
        String routingKey = "";
        String queueName = "";
        boolean autoAck = true;
        DeliverCallback deliveryCallback = (consumerTag, delivery) -> {
        };
        CancelCallback cancelCallback = (consumerTag) -> {
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

        public Builder withDeliveryCallback(DeliverCallback deliveryCallback) {
            this.deliveryCallback = deliveryCallback;
            return this;
        }

        public Builder withCancelCallback(CancelCallback cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        public ExchangeConsumer build() throws IOException, TimeoutException {
            ExchangeConsumer consumer = new ExchangeConsumer();
            consumer.exchangeName = exchangeName;
            consumer.exchangeType = exchangeType;
            consumer.routingKey = routingKey;
            consumer.autoAck = autoAck;
            consumer.deliveryCallback = deliveryCallback;
            consumer.cancelCallback = cancelCallback;

            Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
            consumer.channel = con.createChannel();
            consumer.channel.exchangeDeclare(exchangeName, exchangeType);

            consumer.queueName = consumer.channel.queueDeclare().getQueue();
            consumer.channel.queueBind(queueName, exchangeName, routingKey);
            return consumer;
        }
    }
}
