package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CouponDto {
    private String code;
    @ApiModelProperty(value = "(0) Money" +
            "<br>(1) Points" +
            "<br>(2) Shop" +
            "<br>(3) City hall", required = true)
    private Integer awardType;
    private Long redemptionDate;
    @ApiModelProperty(required = true)
    private Long emissionDate;
    private Long refundDate;
    @ApiModelProperty(required = true)
    private Long expireDate;
    private String name;
    @ApiModelProperty(required = true)
    private BigDecimal value;
    @ApiModelProperty(required = true)
    private String uid;
    @ApiModelProperty(required = true)
    private Long organizationId;
    @ApiModelProperty(value = "Dati dello shop dove è stato consumato il coupon", position = 1)
    private Long shopId;
    @ApiModelProperty(position = 1)
    private String shopName;
    @ApiModelProperty(position = 1)
    private String shopLogo;
    @ApiModelProperty(value = "Articolo ottenuto tramite il coupon", position = 2)
    private Long articleId;
    @ApiModelProperty(position = 2)
    private Long articleTitle;
    @ApiModelProperty(position = 2)
    private String articleImage;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getRedemptionDate() {
        return redemptionDate;
    }

    public void setRedemptionDate(Long redemptionDate) {
        this.redemptionDate = redemptionDate;
    }

    public Long getEmissionDate() {
        return emissionDate;
    }

    public void setEmissionDate(Long emissionDate) {
        this.emissionDate = emissionDate;
    }

    public Long getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(Long refundDate) {
        this.refundDate = refundDate;
    }

    public Long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Long expireDate) {
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopLogo() {
        return shopLogo;
    }

    public void setShopLogo(String shopLogo) {
        this.shopLogo = shopLogo;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(Long articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleImage() {
        return articleImage;
    }

    public void setArticleImage(String articleImage) {
        this.articleImage = articleImage;
    }

    public Integer getAwardType() {
        return awardType;
    }

    public void setAwardType(Integer awardType) {
        this.awardType = awardType;
    }
}
