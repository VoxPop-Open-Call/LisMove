package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AwardPositionDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal value;
    private Integer type;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String address;
    private String number;
    private Long city;
    private BigDecimal radius;
    private Long timestamp;
    private String uid;
    private String username;
    private Long startDate;
    private Long endDate;
    private Long organization;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public BigDecimal getRadius() {
        return radius;
    }

    public void setRadius(BigDecimal radius) {
        this.radius = radius;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getOrganization() {
        return organization;
    }

    public void setOrganization(Long organization) {
        this.organization = organization;
    }
}
