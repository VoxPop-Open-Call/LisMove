package net.nextome.lismove.rest.dto;

import java.math.BigDecimal;

public class AchievementUserPositionDto {
    private String user;
    private String username;
    private BigDecimal score;
    private Boolean fullfilled;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Boolean getFullfilled() {
        return fullfilled;
    }

    public void setFullfilled(Boolean fullfilled) {
        this.fullfilled = fullfilled;
    }
}