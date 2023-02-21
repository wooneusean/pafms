package my.edu.apu.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ExchangePublisher implements Runnable {

    String exchangeName;
    Channel channel;
    BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    String routingKey = "";
    boolean mandatory = false;
    Consumer<ExchangePublisher> messageGenerator = (publisher) -> {
    };
    AMQP.BasicProperties basicProperties = null;

    public ExchangePublisher() {
    }

    public void publish(byte[] data) throws IOException {
        channel.basicPublish(
                exchangeName,
                routingKey,
                mandatory,
                basicProperties,
                data
        );
    }


    @Override
    public void run() {
        messageGenerator.accept(this);
    }

    public static class Builder {
        String exchangeName;
        Channel channel;
        BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
        String routingKey = "";
        boolean mandatory = false;
        Consumer<ExchangePublisher> messageGenerator = (publisher) -> {
        };
        AMQP.BasicProperties basicProperties = null;

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

        public Builder withMandatory(boolean mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        public Builder withMessageGenerator(Consumer<ExchangePublisher> messageGenerator) {
            this.messageGenerator = messageGenerator;
            return this;
        }

        public Builder withBasicProperties(AMQP.BasicProperties basicProperties) {
            this.basicProperties = basicProperties;
            return this;
        }

        public ExchangePublisher build() throws IOException, TimeoutException {
            ExchangePublisher publisher = new ExchangePublisher();
            publisher.exchangeName = exchangeName;
            publisher.exchangeType = exchangeType;
            publisher.routingKey = routingKey;
            publisher.mandatory = mandatory;
            publisher.messageGenerator = messageGenerator;
            publisher.basicProperties = basicProperties;

            Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
            publisher.channel = con.createChannel();
            publisher.channel.exchangeDeclare(exchangeName, exchangeType);
            return publisher;
        }
    }
}
