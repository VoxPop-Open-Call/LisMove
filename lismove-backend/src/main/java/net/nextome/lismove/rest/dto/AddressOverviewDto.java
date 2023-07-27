package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AddressOverviewDto {
    private Long id;
    private String address;
    private String number;
    private Long city;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private Double tolerance;
    private Long startAssociation;
    private Long endAssociation;

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

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public Long getStartAssociation() {
        return startAssociation;
    }

    public void setStartAssociation(Long startAssociation) {
        this.startAssociation = startAssociation;
    }

    public Long getEndAssociation() {
        return endAssociation;
    }

    public void setEndAssociation(Long endAssociation) {
        this.endAssociation = endAssociation;
    }
}
