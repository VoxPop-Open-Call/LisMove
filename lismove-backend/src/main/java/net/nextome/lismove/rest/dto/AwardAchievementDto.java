package net.nextome.lismove.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AwardAchievementDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal value;
    private Integer type;
    private String imageUrl;
    private Long achievement;

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

    public Long getAchievement() {
        return achievement;
    }

    public void setAchievement(Long achievement) {
        this.achievement = achievement;
    }
}