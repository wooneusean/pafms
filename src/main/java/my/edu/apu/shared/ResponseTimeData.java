package my.edu.apu.shared;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class ResponseTimeData implements Serializable {
    String sensor;
    String actuator;
    Long sensorToControlResponseTime;
    Long controlToActuatorResponseTime;

    public Long getOverallResponseTime() {
        return sensorToControlResponseTime + controlToActuatorResponseTime;
    }
}
