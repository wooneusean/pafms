package my.edu.apu;

import jakarta.persistence.EntityManager;
import my.edu.apu.rabbitmq.HibernateSessionProvider;
import my.edu.apu.shared.AirplaneState;

public class Main {
    public static void main(String[] args) {
        // Seed data
        AirplaneState state = new AirplaneState();
        state.setId(1L);
        ////////////////////////////////
        // Sensors
        ////////////////////////////////

        // in km/h
        state.setSpeed(350);

        // in feet
        state.setAltitude(25000);

        // in degrees, ideal is 0
        state.setDirection(0);

        // Ideal pressure, too much cabin pressure is around 12000 I guess?
        state.setCabinPressure(6000);

        ////////////////////////////////
        // Actuators
        ////////////////////////////////

        // % of throttle
        // How this affects speed:
        //  - ((engineThrottle / 100) * 400) - randomDragValue (100 - 400)
        //  - That value is added to the speed
        state.setEngineThrottle(50);

        // 0 deg means straight
        state.setWingAngle(0);
        state.setTailAngle(0);

        state.setLandingGearDeployed(false);
        state.setOxygenMasksDeployed(false);

//        ResponseTimeData rtd = ResponseTimeData
//                .builder()
//                .actuator("test-actuator")
//                .sensor("test-sensor")
//                .sensorToControlResponseTime(120L)
//                .controlToActuatorResponseTime(50L)
//                .build();

        EntityManager em = HibernateSessionProvider.getInstance().getEntityManager();
        em.getTransaction().begin();
        em.persist(state);
//        em.persist(rtd);
        em.getTransaction().commit();
        em.close();
    }
}