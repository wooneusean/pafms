package my.edu.apu.shared;

public class EnvironmentState {
    public int altitude, direction, cabinPressure, speed;

    public EnvironmentState(int altitude, int direction, int cabinPressure, int speed) {
        this.altitude = altitude;
        this.direction = direction;
        this.cabinPressure = cabinPressure;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "{\"EnvironmentState\":{" +
               "\"altitude\":" + altitude +
               ", \"direction\":" + direction +
               ", \"cabinPressure\":" + cabinPressure +
               ", \"speed\":" + speed +
               "}}";
    }
}
