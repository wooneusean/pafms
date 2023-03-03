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

public class CabinPressureSensor {
    static int currentIterations = 0;

    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangePublisher directionPublisher = new ExchangePublisher.Builder()
                .withExchangeName(Constants.SENSORY_TO_CONTROL_EXCHANGE_NAME)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withTargetRoutingKey(Constants.FLIGHT_CONTROL_ROUTING_KEY)
                .withMessageGenerator(publisher -> {
                    currentIterations++;

                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();
                    AirplaneState state = em.find(AirplaneState.class, 1);
                    int newCabinPressure = state.getCabinPressure();
                    if (currentIterations > 20) {
                        newCabinPressure -= Math.floor((Math.random() * 1000) - 500);
                    } else {
                        newCabinPressure += Math.floor((Math.random() * 100) - 50);
                    }
                    state.setCabinPressure(newCabinPressure);
                    em.getTransaction().commit();

                    System.out.println(
                            "[.] Sending cabin pressure: " +
                            newCabinPressure +
                            " to " +
                            publisher.getTargetRoutingKey()
                    );

                    try {
                        publisher.publish(new SensoryToControlPacket(
                                Constants.CABIN_PRESSURE_SENSOR_ROUTING_KEY,
                                newCabinPressure,
                                System.currentTimeMillis()
                        ).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(directionPublisher, 0, 1, TimeUnit.SECONDS);
    }
}
