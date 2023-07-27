package net.nextome.lismove.models;

import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public class Address extends AuditableEntity{
    private String address;
    private String number;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    private City city;

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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public boolean fullAddressNotNull() {
        return address != null && city != null;
    }

    public boolean coordinatesNotNull() {
        return latitude != null && longitude != null;
    }

    public boolean equals(Address a) {
        return this.formatAddress().equals(a.formatAddress()) && (this.latitude != null && a.getLatitude() != null && this.latitude.equals(a.getLatitude())) && (this.longitude != null && a.getLongitude() != null && this.longitude.equals(a.getLongitude()));
    }

    //TODO crea test
    public String formatAddress() {
        return (address != null ? address : "")
                + (number != null ? ", " + number : "" )
                + (city != null ?  ", " + city.getCap() + " " + city.getCity() + " " + city.getProvince() : "");
    }
}
