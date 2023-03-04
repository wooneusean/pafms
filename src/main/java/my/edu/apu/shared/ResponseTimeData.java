package my.edu.apu.shared;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ResponseTimes")
@Getter
@Setter
@Builder
public class ResponseTimeData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column
    @CreationTimestamp
    LocalDateTime createdAt;
    @Column
    @UpdateTimestamp
    LocalDateTime updatedAt;
    @Column
    String sensor;
    @Column
    String actuator;
    @Column
    Long sensorToControlResponseTime;
    @Column
    Long controlToActuatorResponseTime;

    @Transient
    public Long getOverallResponseTime() {
        return sensorToControlResponseTime + controlToActuatorResponseTime;
    }
}
