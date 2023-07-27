package net.nextome.lismove.models;


import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @SequenceGenerator(name = "sensorseq", sequenceName = "sensor_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sensorseq")
    private Long id;
    private String uuid;
    private String firmware;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime startAssociation;
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime endAssociation;
    private String bikeType;
    @Column(columnDefinition = "numeric(10,5)")
    private BigDecimal wheelDiameter;
    @ColumnDefault("1")
    private BigDecimal hubCoefficient = BigDecimal.ONE;
    private Boolean stolen;
    private String name;

    @OneToOne
    private Ugo ugo;

    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public LocalDateTime getStartAssociation() {
        return startAssociation;
    }

    public void setStartAssociation(LocalDateTime startAssociation) {
        this.startAssociation = startAssociation;
    }

    public LocalDateTime getEndAssociation() {
        return endAssociation;
    }

    public void setEndAssociation(LocalDateTime endAssociation) {
        this.endAssociation = endAssociation;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    public BigDecimal getWheelDiameter() {
        return wheelDiameter;
    }

    public void setWheelDiameter(BigDecimal wheelDiameter) {
        this.wheelDiameter = wheelDiameter;
    }

    public BigDecimal getHubCoefficient() {
        return hubCoefficient;
    }

    public void setHubCoefficient(BigDecimal hubCoefficient) {
        this.hubCoefficient = hubCoefficient;
    }

    public Boolean getStolen() {
        return stolen;
    }

    public void setStolen(Boolean stolen) {
        this.stolen = stolen;
    }

    public Ugo getUgo() {
        return ugo;
    }

    public void setUgo(Ugo ugo) {
        this.ugo = ugo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
