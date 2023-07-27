package net.nextome.lismove.models;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ugos")
public class Ugo {

    @Id
    private Long id;
    private String uuid;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime activationDate;
    private String eSimCode;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime feeDeadline;
    private Integer batteryCycles;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime lastWarning;


    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Sensor sensor;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }

    public String geteSimCode() {
        return eSimCode;
    }

    public void seteSimCode(String eSimCode) {
        this.eSimCode = eSimCode;
    }

    public LocalDateTime getFeeDeadline() {
        return feeDeadline;
    }

    public void setFeeDeadline(LocalDateTime feeDeadline) {
        this.feeDeadline = feeDeadline;
    }

    public Integer getBatteryCycles() {
        return batteryCycles;
    }

    public void setBatteryCycles(Integer batteryCycles) {
        this.batteryCycles = batteryCycles;
    }

    public LocalDateTime getLastWarning() {
        return lastWarning;
    }

    public void setLastWarning(LocalDateTime lastWarning) {
        this.lastWarning = lastWarning;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }
}
