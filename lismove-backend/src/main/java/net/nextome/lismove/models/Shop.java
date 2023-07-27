package net.nextome.lismove.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "shops")
public class Shop {

    @Id
    @SequenceGenerator(name = "shopsseq", sequenceName = "shops_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "shopsseq")
    private Long id;
    private String name;
    private String website;
    private String claim;
    private String description;
    @Column(columnDefinition = "varchar")
    private String logo;
    @JsonRawValue
    @Column(columnDefinition = "varchar")
    private String images;
    private Boolean isEcommerce;
    private String address;
    private String number;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private Boolean isPrimary;

    @ManyToOne
    private Vendor vendor;

    @ManyToOne
    private City city;

    @ManyToMany
    private Set<Category> categories;

    @OneToMany(mappedBy = "shop")
    private List<Article> articles;

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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Boolean getEcommerce() {
        return isEcommerce;
    }

    public void setEcommerce(Boolean ecommerce) {
        isEcommerce = ecommerce;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean primary) {
        this.isPrimary = primary;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
