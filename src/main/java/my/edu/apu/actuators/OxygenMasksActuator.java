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
import java.util.concurrent.TimeoutException;

public class OxygenMasksActuator {
    public static void main(String[] args) throws IOException, TimeoutException {
        ExchangeConsumer oxygenMaskConsumer = new ExchangeConsumer.Builder()
                .withExchangeName(Constants.CONTROL_TO_ACTUATOR_EXCHANGE)
                .withExchangeType(BuiltinExchangeType.DIRECT)
                .withRoutingKey(Constants.OXYGEN_MASKS_ROUTING_KEY)
                .withDeliveryCallback(c -> (s, delivery) -> {
                    ControlToActuatorPacket packet = Publishable.fromBytes(delivery.getBody());

                    System.out.printf(
                            "[.] Setting oxygen masks to: %s %n",
                            packet.getValue() == 0 ? "hidden" : "deployed"
                    );

                    EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
                    em.getTransaction().begin();
                    AirplaneState state = em.find(AirplaneState.class, 1);
                    state.setOxygenMasksDeployed(packet.getValue() == 1);
                    em.getTransaction().commit();
                })
                .build();

        new Thread(oxygenMaskConsumer).start();
    }
}
