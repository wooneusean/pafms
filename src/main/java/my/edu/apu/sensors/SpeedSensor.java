package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import jakarta.persistence.EntityManager;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.HibernateSessionProvider;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.SensoryToControlPacket;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpeedSensor {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher speedPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();
                    AirplaneState state = em.find(AirplaneState.class, 1);
                    int newSpeed = (int) (state.getSpeed() + Math.floor(Math.random() * -100) +
                                          Math.floor(state.getEngineThrottle() / 100.0 * 100));

                    if (newSpeed <= 0) {
                        newSpeed = 0;
                    }

                    state.setSpeed(newSpeed);
                    em.getTransaction().commit();

                    System.out.println(
                            "[.] Sending speed: " +
                            newSpeed +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(new SensoryToControlPacket(
                                Constants.SPEED_SENSOR_ROUTING_KEY,
                                newSpeed
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(speedPublisher, 0, 1, TimeUnit.SECONDS);
    }
}
