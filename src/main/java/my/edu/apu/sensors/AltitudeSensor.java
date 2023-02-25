package my.edu.apu.sensors;

import com.rabbitmq.client.BuiltinExchangeType;
import jakarta.persistence.EntityManager;
import my.edu.apu.rabbitmq.ExchangePublisher;
import my.edu.apu.rabbitmq.HibernateSessionProvider;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.SensoryToControlPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class AltitudeSensor {
    public static void main(String[] args) throws IOException, TimeoutException {
        Logger logger = LoggerFactory.getLogger(AltitudeSensor.class);
        ExchangePublisher altitudePublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();
                    AirplaneState state = em.find(AirplaneState.class, 1);
                    int newAltitude = (int) (state.getAltitude() + Math.floor(Math.random() * -200) +
                                             Math.floor(state.getWingAngle() / 45.0 * 600));

                    if (newAltitude <= 0) {
                        newAltitude = 0;
                    }

                    state.setAltitude(newAltitude);
                    em.getTransaction().commit();

                    System.out.println(
                            "[.] Sending altitude: " +
                            newAltitude +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(new SensoryToControlPacket(
                                Constants.ALTITUDE_SENSOR_ROUTING_KEY,
                                newAltitude
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(altitudePublisher, 0, 1, TimeUnit.SECONDS);
    }
}
