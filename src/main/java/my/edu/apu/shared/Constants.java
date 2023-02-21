package my.edu.apu.shared;

public class Constants {
    public static final String FLIGHT_CONTROLLER_ROUTING_KEY = "flight-controller";
    public static final String ALTITUDE_CONSUMER_TOPIC = "sensor.altitude";
    public static final String CABIN_CONSUMER_TOPIC = "sensor.cabin";
    public static final String SENSORY_TO_CONTROLLER_EXCHANGE_NAME = "sensory-to-controller";
    public static final String AIRPLANE_STATE_UPDATER_EXCHANGE = "airplane-state-exchange";
    public static final String AIRPLANE_STATE_UPDATE_ROUTING_KEY = "airplane-state";
    public static final String ENGINE_THROTTLE = "engineThrottle";
    public static final String WING_ANGLE = "wingAngle";
    public static final String TAIL_ANGLE = "tailAngle";
    public static final String LANDING_GEAR_DEPLOYED = "landingGearDeployed";
    public static final String OXYGEN_MASKS_DEPLOYED = "oxygenMasksDeployed";
}
