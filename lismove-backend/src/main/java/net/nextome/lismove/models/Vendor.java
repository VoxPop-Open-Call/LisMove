package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @SequenceGenerator(name = "vendorsseq", sequenceName = "vendors_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "vendorsseq")
    private Long id;
    @Column(columnDefinition = "numeric(10,5)")
    private BigDecimal totalValueReleasedCoupon;
    private Integer numberAssignedCoupon;
    private String businessName;
    private String address;
    private String vatNumber;
    private String iban;
    private String bic;
    private Boolean isVisible;
    private BigDecimal points;

    @OneToOne
    private User user;

    @ManyToOne
    private Organization organization;

    @OneToMany(mappedBy = "vendor")
    private List<Shop> shops;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalValueReleasedCoupon() {
        return totalValueReleasedCoupon;
    }

    public void setTotalValueReleasedCoupon(BigDecimal totalValueReleasedCoupon) {
        this.totalValueReleasedCoupon = totalValueReleasedCoupon;
    }

    public Integer getNumberAssignedCoupon() {
        return numberAssignedCoupon;
    }

    public void setNumberAssignedCoupon(Integer numberAssignedCoupon) {
        this.numberAssignedCoupon = numberAssignedCoupon;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Shop> getShops() {
        return shops;
    }

    public void setShops(List<Shop> shops) {
        this.shops = shops;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }
}
