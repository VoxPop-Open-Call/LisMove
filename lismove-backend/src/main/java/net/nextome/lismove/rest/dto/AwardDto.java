package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ApiModel(description = "Contiene tutti i campi di tutti i tipi di premi in modo tale da mapparli in un solo dto." +
        "<br>L'oggetto coupon esiste solo se si tratta di un premio in denaro (type = 0) o da ritirare in comune (type = 3)")
public class AwardDto {
    @ApiModelProperty(required = true)
    private String name;
    private String description;
    @ApiModelProperty(value = "Valore in euro o punti", required = true)
    private BigDecimal value;
    @ApiModelProperty(value = "Indica se value è un valore in:" +
            "<br>(0) euro" +
            "<br>(1) punti" +
            "<br>(2) oggetto da ritirare in comune", required = true)
    private Integer type;
    private String imageUrl;
    @ApiModelProperty(value = "Timestamp di vincita premio", required = true)
    private Long timestamp;
    @ApiModelProperty(value = "Organizzazione che ha emesso il premio")
    private Long organizationId;
    // AwardAchievement
    @ApiModelProperty(value = "Valorizzato se AwardAchievement", position = 1)
    private Long achievementId;
    // AwardPosition
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private Double latitude;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private Double longitude;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private String address;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private String number;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private Long city;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private BigDecimal radius;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private String uid;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private String username;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private Long startDate;
    @ApiModelProperty(value = "Valorizzato se AwardPosition", position = 2)
    private Long endDate;
    // AwardRanking
    @ApiModelProperty(value = "Valorizzato se AwardRanking", position = 3)
    private Long rankingId;
    @ApiModelProperty(value = "Valorizzato se AwardRanking", position = 3)
    private Integer position;
    @ApiModelProperty(value = "Valorizzato se AwardRanking", position = 3)
    private String range;
    // AwardCustom
    @ApiModelProperty(value = "Valorizzato se AwardCustom" +
            "<br>Indica chi ha creato l'AwardCustom; può assumere i seguenti valori:" +
            "<br>(0) CREATED_BY_ORGANIZATION" +
            "<br>(1) CREATED_BY_ADMIN" +
            "<br>(2) CREATED_BY_REFUNDS", position = 4)
    private Integer issuer;

    @ApiModelProperty(position = 5)
    private CouponDto coupon;

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

    public Long getAchievementId() {
        return achievementId;
    }

    public void setAchievementId(Long achievementId) {
        this.achievementId = achievementId;
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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getRankingId() {
        return rankingId;
    }

    public void setRankingId(Long rankingId) {
        this.rankingId = rankingId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public CouponDto getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponDto coupon) {
        this.coupon = coupon;
    }

    public Integer getIssuer() {
        return issuer;
    }

    public void setIssuer(Integer issuer) {
        this.issuer = issuer;
    }
}