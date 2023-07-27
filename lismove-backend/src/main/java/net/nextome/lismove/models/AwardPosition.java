package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "award_positions")
public class AwardPosition extends Award {
    @Id
    @SequenceGenerator(name = "awardpositionsseq", sequenceName = "awardpositions_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "awardpositionsseq")
    private Long id;
    private String address;
    private String number;
    private Double latitude;
    private Double longitude;
    private BigDecimal radius; //km
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    private Organization organization;
    @ManyToOne
    private City city;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getRadius() {
        return radius;
    }

    public void setRadius(BigDecimal radius) {
        this.radius = radius;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
