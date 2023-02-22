package my.edu.apu.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExchangePublisher implements Runnable {
    protected HashMap<String, String> userData = new HashMap<>();
    protected boolean mandatory = false;
    protected String exchangeName;
    protected Channel channel;
    protected String routingKey = "";
    protected BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
    protected Consumer<ExchangePublisher> messageGenerator = (publisher) -> {
    };
    protected AMQP.BasicProperties basicProperties = null;

    public ExchangePublisher() {
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public HashMap<String, String> getUserData() {
        return userData;
    }

    public void setUserData(HashMap<String, String> userData) {
        this.userData = userData;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public BuiltinExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(BuiltinExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public Consumer<ExchangePublisher> getMessageGenerator() {
        return messageGenerator;
    }

    public void setMessageGenerator(Consumer<ExchangePublisher> messageGenerator) {
        this.messageGenerator = messageGenerator;
    }

    public AMQP.BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public void setBasicProperties(AMQP.BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
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
        BuiltinExchangeType exchangeType = BuiltinExchangeType.FANOUT;
        String routingKey = "";
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

        public Builder withBasicPropertiesProvider(Function<ExchangePublisher, AMQP.BasicProperties> basicPropertiesProvider) {
            this.basicPropertiesProvider = basicPropertiesProvider;
            return this;
        }

        public ExchangePublisher build() throws IOException, TimeoutException {
            ExchangePublisher publisher = new ExchangePublisher();
            publisher.exchangeName = exchangeName;
            publisher.exchangeType = exchangeType;
            publisher.routingKey = routingKey;
            publisher.mandatory = mandatory;
            publisher.messageGenerator = messageGenerator;

            Connection con = RabbitMQConnectionProvider.getInstance().getConnection();
            publisher.channel = con.createChannel();
            publisher.channel.exchangeDeclare(exchangeName, exchangeType);

            publisher.basicProperties = basicPropertiesProvider.apply(publisher);
            return publisher;
        }
    }
}
