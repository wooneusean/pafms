package my.edu.apu.actuators;

import com.rabbitmq.client.BuiltinExchangeType;
import jakarta.persistence.EntityManager;
import my.edu.apu.rabbitmq.ExchangeConsumer;
import my.edu.apu.rabbitmq.HibernateSessionProvider;
import my.edu.apu.rabbitmq.Publishable;
import my.edu.apu.shared.AirplaneState;
import my.edu.apu.shared.Constants;
import my.edu.apu.shared.ControlToActuatorPacket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TailFlapsActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer tailFlapInstructionConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.TAIL_FLAPS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());
                    System.out.printf(
                            "[.] Setting tail flap angle to '%s'%n",
                            value
                    );
                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();
                    AirplaneState state = em.find(AirplaneState.class, 1);
                    state.setTailAngle(value);
                    em.getTransaction().commit();
                })
                .build();

        new Thread(tailFlapInstructionConsumer).start();
    }
}
