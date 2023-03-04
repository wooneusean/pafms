package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import jakarta.persistence.EntityManager;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.HibernateSessionProvider;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.ControlToActuatorPacket;
import my.edu.apu.shared.ResponseTimeData;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class LandingGearActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer landingGearConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.LANDING_GEAR_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());

                    String landingGearState = packet.getValue() == 1 ? "DOWN" : "UP";
                    System.out.println("[i] Landing gear is set to " + landingGearState);

                    ResponseTimeData rtd = ResponseTimeData
                            .builder()
                            .actuator(Constants.LANDING_GEAR_ROUTING_KEY)
                            .sensor(packet.getSensor())
                            .sensorToControlResponseTime(packet.getSensorToControlResponseTime())
                            .controlToActuatorResponseTime(
                                    System.currentTimeMillis() - packet.getTimestampFromControl()
                            )
                            .build();

                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();

                    AirplaneState state = em.find(AirplaneState.class, 1);
                    state.setLandingGearDeployed(packet.getValue() != 0);
                    em.persist(rtd);

                    em.getTransaction().commit();
                })
                .build();

        new Thread(landingGearConsumer).start();
    }
}
