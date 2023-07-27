package net.nextome.lismove.rest.dto;

import java.util.Set;

public class ShopDto {
    private Long id;
    private String address;
    private String claim;
    private String description;
    private Set<String> images;
    private Boolean isEcommerce;
    private Boolean isPrimary;
    private Double latitude;
    private String logo;
    private Double longitude;
    private String name;
    private String number;
    private String phone;
    private String website;
    private Set<Long> categories;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsEcommerce() {
        return isEcommerce;
    }

    public void setIsEcommerce(Boolean ecommerce) {
        isEcommerce = ecommerce;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public Set<Long> getCategories() {
        return categories;
    }

    public void setCategories(Set<Long> categories) {
        this.categories = categories;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getImages() {
        return images;
    }

    public void setImages(Set<String> images) {
        this.images = images;
    }
}
