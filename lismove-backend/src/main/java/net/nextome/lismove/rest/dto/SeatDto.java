package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

//@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SeatDto {
    private Long id;
    private String name;
    private String address;
    private String number;
    private Long city;
    private String cityName;
    private Long organization;
    private Double latitude;
    private Double longitude;
    private Boolean validated;
    private BigDecimal destinationTolerance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getCity() {
        return city;
    }

    public void setCity(Long city) {
        this.city = city;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Long getOrganization() {
        return organization;
    }

    public void setOrganization(Long organization) {
        this.organization = organization;
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

    public Boolean getValidated() {
        return validated;
    }

    public void setValidated(Boolean validated) {
        this.validated = validated;
    }

    public BigDecimal getDestinationTolerance() {
        return destinationTolerance;
    }

    public void setDestinationTolerance(BigDecimal destinationTolerance) {
        this.destinationTolerance = destinationTolerance;
    }
}
