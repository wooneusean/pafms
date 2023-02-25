package my.edu.apu.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import my.edu.apu.rabbitmq.HibernateSessionProvider;

import java.io.Serializable;

@Entity
@Table(name = "AirplaneState")
@Getter
@Setter
public class AirplaneState implements Serializable {
    @Column
    protected int engineThrottle;
    @Column
    protected int wingAngle;
    @Column
    protected int tailAngle;
    @Column
    protected int altitude;
    @Column
    protected int direction;
    @Column
    protected int cabinPressure;
    @Column
    protected int speed;
    @Column
    protected boolean landingGearDeployed;
    @Column
    protected boolean oxygenMasksDeployed;
    @Id
    protected Long id;

    public AirplaneState() {
    }
}
