package my.edu.apu.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnectionProvider {
    private static volatile RabbitMQConnectionProvider instance;
    ConnectionFactory connectionFactory = new ConnectionFactory();
    Connection connection;

    private RabbitMQConnectionProvider() throws IOException, TimeoutException {
        connection = connectionFactory.newConnection();
    }

    public static RabbitMQConnectionProvider getInstance() throws IOException, TimeoutException {
        RabbitMQConnectionProvider result = instance;
        if (result != null) {
            return result;
        }

        synchronized (RabbitMQConnectionProvider.class) {
            if (instance == null) {
                instance = new RabbitMQConnectionProvider();
            }
            return instance;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
