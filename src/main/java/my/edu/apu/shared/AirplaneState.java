package my.edu.apu.shared;

public class AirplaneState extends Publishable {
    public float engineThrottle, wingAngle, tailAngle;
    public boolean landingGearDeployed, oxygenMasksDeployed;

    public AirplaneState(
            float engineThrottle,
            float wingAngle,
            float tailAngle,
            boolean landingGearDeployed,
            boolean oxygenMasksDeployed
    ) {
        this.engineThrottle = engineThrottle;
        this.wingAngle = wingAngle;
        this.tailAngle = tailAngle;
        this.landingGearDeployed = landingGearDeployed;
        this.oxygenMasksDeployed = oxygenMasksDeployed;
    }

    @Override
    public String toString() {
        return "{\"AirplaneState\":{" +
               "\"engineThrottle\":" + engineThrottle +
               ", \"wingAngle\":" + wingAngle +
               ", \"tailAngle\":" + tailAngle +
               ", \"landingGearDeployed\":" + landingGearDeployed +
               ", \"oxygenMasksDeployed\":" + oxygenMasksDeployed +
               "} " + super.toString();
    }
}
