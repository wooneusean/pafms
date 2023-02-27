package my.edu.apu.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@Setter
public class ExchangePublisher implements Runnable {
    protected boolean mandatory = false;
    protected String exchangeName;
    protected Channel channel;
    protected String targetRoutingKey = "";
    protected BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    protected Consumer<ExchangePublisher> messageGenerator = (publisher) -> {
    };
    protected AMQP.BasicProperties basicProperties = null;

    public ExchangePublisher() {
    }

    public void publish(byte[] data) throws IOException {
        channel.basicPublish(
                exchangeName,
                targetRoutingKey,
                mandatory,
                basicProperties,
                data
        );
        System.out.printf(
                "[.] Sent out a message of %d bytes to [%s] on exchange [%s]%n",
                data.length,
                targetRoutingKey,
                exchangeName
        );
    }

    public void publish(byte[] data, String consumerRoutingKey) throws IOException {
        channel.basicPublish(
                exchangeName,
                consumerRoutingKey,
                mandatory,
                basicProperties,
                data
        );
        System.out.printf(
                "[.] Sent out a message of %d bytes to [%s] on exchange [%s]%n",
                data.length,
                consumerRoutingKey,
                exchangeName
        );
    }


    @Override
    public void run() {
        messageGenerator.accept(this);
    }

    public static class Builder {
        String exchangeName;
        BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
        String targetRoutingKey = "";
        boolean mandatory = false;
        Consumer<ExchangePublisher> messageGenerator = (publisher) -> {
        };
        Function<ExchangePublisher, AMQP.BasicProperties> basicPropertiesProvider = builder -> null;

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

        public Builder withTargetRoutingKey(String routingKey) {
            this.targetRoutingKey = routingKey;
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

        public Builder withBasicPropertiesProvider(Function<ExchangePublisher, AMQP.BasicProperties> basicPropertiesProvider) {
            this.basicPropertiesProvider = basicPropertiesProvider;
            return this;
        }

        public ExchangePublisher build() throws IOException, TimeoutException {
            ExchangePublisher publisher = new ExchangePublisher();
            publisher.setExchangeName(exchangeName);
            publisher.setExchangeType(exchangeType);
            publisher.setTargetRoutingKey(targetRoutingKey);
            publisher.setMandatory(mandatory);
            publisher.setMessageGenerator(messageGenerator);

            Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
            publisher.setChannel(con.createChannel());
            publisher.getChannel().exchangeDeclare(exchangeName, exchangeType);

            publisher.setBasicProperties(basicPropertiesProvider.apply(publisher));
            return publisher;
        }
    }
}
