package my.edu.apu.rabbitmq;

public class AirplaneStateUpdaterPacket extends Publishable {
    protected String actuatorName;
    protected int actuatorValue;

    public String getActuatorName() {
        return actuatorName;
    }

    public void setActuatorName(String actuatorName) {
        this.actuatorName = actuatorName;
    }

    public int getActuatorValue() {
        return actuatorValue;
    }

    public void setActuatorValue(int actuatorValue) {
        this.actuatorValue = actuatorValue;
    }

    @Override
    public String toString() {
        return "{\"AirplaneStateUpdaterPacket\":{" +
               "\"actuatorName\":\"" + actuatorName + '\"' +
               ", \"actuatorValue\":" + actuatorValue +
               "} " + super.toString();
    }
}
