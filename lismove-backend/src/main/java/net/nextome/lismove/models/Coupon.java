package net.nextome.lismove.models;

import net.nextome.lismove.models.enums.AwardType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @SequenceGenerator(name = "couponsseq", sequenceName = "coupons_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "couponsseq")
    private Long id;
    private String code;
    private AwardType awardType;
    private LocalDateTime redemptionDate;
    private LocalDateTime emissionDate;
    private LocalDateTime refundDate;
    private LocalDate expireDate;
    private String name; //da rispettivo award
    private String description;
    private BigDecimal value; //da rispettivo award

    @ManyToOne
    private User user;

    @ManyToOne
    private Organization organization;

    @ManyToOne
    private Shop shop;

    @ManyToOne
    private Article article;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getRedemptionDate() {
        return redemptionDate;
    }

    public void setRedemptionDate(LocalDateTime redemptionDate) {
        this.redemptionDate = redemptionDate;
    }

    public LocalDateTime getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(LocalDateTime emissionDate) {
        this.emissionDate = emissionDate;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public AwardType getAwardType() {
        return awardType;
    }

    public void setAwardType(AwardType awardType) {
        this.awardType = awardType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
